package org.cmdbuild.dao.driver.postgres;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.cmdbuild.dao.driver.postgres.Utils.tableNameToDomainName;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.Utils.CommentMapper;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBAttribute.AttributeMetadata;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBClass.ClassMetadata;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBDomain.DomainMetadata;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.function.DBFunction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

public class EntryTypeCommands {

	private static final Pattern COMMENT_PATTERN = Pattern.compile("(([A-Z0-9]+): ([^|]*))*");

	private final JdbcTemplate jdbcTemplate;

	EntryTypeCommands(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/*
	 * Classes
	 */

	public List<DBClass> findAllClasses() {
		final ClassTreeBuilder rch = new ClassTreeBuilder();
		jdbcTemplate
				.query("SELECT table_id, _cm_cmtable(table_id) AS table_name, _cm_parent_id(table_id) AS parent_id,"
						+ " _cm_comment_for_table_id(table_id) AS table_comment FROM _cm_class_list() AS table_id", rch);
		return rch.getResult();
	}

	private class ClassTreeBuilder implements RowCallbackHandler {

		private class ClassAndParent {

			public final DBClass dbClass;
			public final Object parentId;

			public ClassAndParent(final DBClass dbClass, final Object parentId) {
				this.dbClass = dbClass;
				this.parentId = parentId;
			}

		}

		private final Map<Object, ClassAndParent> classMap = new HashMap<Object, ClassAndParent>();

		@Override
		public void processRow(final ResultSet rs) throws SQLException {
			final Long id = rs.getLong("table_id");
			final String name = rs.getString("table_name");
			final Long parentId = (Long) rs.getObject("parent_id");
			final List<DBAttribute> attributes = userEntryTypeAttributesFor(id);
			final String comment = rs.getString("table_comment");
			final ClassMetadata meta = classCommentToMetadata(comment);
			final DBClass dbClass = new DBClass(name, id, meta, attributes);
			classMap.put(id, new ClassAndParent(dbClass, parentId));
		}

		public List<DBClass> getResult() {
			return linkClasses();
		}

		private List<DBClass> linkClasses() {
			final List<DBClass> allClasses = new ArrayList<DBClass>();
			for (final ClassAndParent cap : classMap.values()) {
				final DBClass child = cap.dbClass;
				if (cap.parentId != null) {
					final DBClass parent = classMap.get(cap.parentId).dbClass;
					child.setParent(parent);
				}
				allClasses.add(child);
			}
			return allClasses;
		}
	}

	public DBClass createClass(final String name, final DBClass parent) {
		return createClass(name, parent, false);
	}

	public DBClass createSuperClass(final String name, final DBClass parent) {
		return createClass(name, parent, true);
	}

	private DBClass createClass(final String name, final DBClass parent, final boolean isSuperclass) {
		final String parentName = (parent != null) ? parent.getName() : null;
		final String classComment = createClassComment(name, isSuperclass);
		final long id = jdbcTemplate.queryForInt( //
				"SELECT cm_create_class(?, ?, ?)", //
				new Object[] { name, parentName, classComment });
		final DBClass newClass = new DBClass( //
				name, //
				id, //
				classCommentToMetadata(classComment), //
				userEntryTypeAttributesFor(id));
		newClass.setParent(parent);
		return newClass;
	}

	private String createClassComment(final String name, final boolean isSuperclass) {
		return format("DESCR: %s|MODE: write|STATUS: active|SUPERCLASS: %s|TYPE: class", name, isSuperclass);
	}

	public void deleteClass(final DBClass dbClass) {
		jdbcTemplate.queryForObject("SELECT cm_delete_class(?)", Object.class, new Object[] { dbClass.getName() });
		dbClass.setParent(null);
	}

	/*
	 * Domains
	 */

	public List<DBDomain> findAllDomains(final PostgresDriver driver) {
		// Exclude Map since we don't need it anymore!
		final List<DBDomain> domainList = jdbcTemplate.query(
				"SELECT domain_id, _cm_cmtable(domain_id) AS domain_name, _cm_comment_for_table_id(domain_id) AS domain_comment" //
						+ " FROM _cm_domain_list() AS domain_id" //
						+ " WHERE domain_id <> '\"Map\"'::regclass", //
				new RowMapper<DBDomain>() {
					@Override
					public DBDomain mapRow(final ResultSet rs, final int rowNum) throws SQLException {
						final Long id = rs.getLong("domain_id");
						final String name = tableNameToDomainName(rs.getString("domain_name"));
						final List<DBAttribute> attributes = userEntryTypeAttributesFor(id);
						final String comment = rs.getString("domain_comment");
						final DomainMetadata meta = domainCommentToMetadata(comment);
						final DBClass class1 = driver.findClassByName(meta.get(DomainMetadata.CLASS_1));
						final DBClass class2 = driver.findClassByName(meta.get(DomainMetadata.CLASS_2));
						final DBDomain domain = DBDomain.newDomain() //
								.withName(name) //
								.withId(id) //
								.withAllMetadata(meta) //
								.withAllAttributes(attributes) //
								.withClass1(class1) //
								.withClass2(class2) //
								.build();
						return domain;
					}
				});
		return domainList;
	}

	public DBDomain createDomain(final DBDriver.DomainDefinition domainDefinition) {
		final String domainComment = domainCommentFrom(domainDefinition);
		final long id = jdbcTemplate.queryForInt("SELECT cm_create_domain(?, ?)", //
				new Object[] { domainDefinition.getName(), domainComment });
		return DBDomain.newDomain() //
				.withName(domainDefinition.getName()) //
				.withId(id) //
				.withAllAttributes(userEntryTypeAttributesFor(id)) //
				// FIXME looks ugly!
				.withAttribute(new DBAttribute(DBRelation._1, new ReferenceAttributeType(), null)) //
				.withAttribute(new DBAttribute(DBRelation._2, new ReferenceAttributeType(), null)) //
				.withAllMetadata(domainCommentToMetadata(domainComment)) //
				.withClass1(domainDefinition.getClass1()) //
				.withClass2(domainDefinition.getClass2()) //
				.build();
	}

	private String domainCommentFrom(final DBDriver.DomainDefinition domainDefinition) {
		// TODO handle more that two classes
		return format(
				"LABEL: %s|DESCRDIR: %s|DESCRINV: %s|MODE: reserved|STATUS: active|TYPE: domain|CLASS1: %s|CLASS2: %s|CARDIN: %s", //
				domainDefinition.getName(), //
				domainDefinition.getDirectDescription(), //
				domainDefinition.getInverseDescription(), //
				domainDefinition.getClass1().getName(), //
				domainDefinition.getClass2().getName(), //
				domainDefinition.getCardinality());
	}

	public void deleteDomain(final DBDomain dbDomain) {
		jdbcTemplate.queryForObject("SELECT cm_delete_domain(?)", Object.class, new Object[] { dbDomain.getName() });
	}

	/*
	 * Attributes
	 */

	/**
	 * Returns user-only entry type attributes (so, not {@code reserved}
	 * attributes).
	 * 
	 * @param entryTypeId
	 *            is the id of he entry type (e.g. {@link DBClass},
	 *            {@link DBDomain}).
	 * 
	 * @return a list of user attributes.
	 */
	private List<DBAttribute> userEntryTypeAttributesFor(final long entryTypeId) {
		// Note: Sort the attributes in the query
		final List<DBAttribute> entityTypeAttributes = jdbcTemplate
				.query("SELECT A.name, _cm_comment_for_attribute(A.cid, A.name) AS comment, _cm_get_attribute_sqltype(A.cid, A.name) AS sql_type" //
						+ " FROM (SELECT C.cid, _cm_attribute_list(C.cid) AS name FROM (SELECT ? AS cid) AS C) AS A" //
						+ " WHERE _cm_read_comment(_cm_comment_for_attribute(A.cid, A.name), 'MODE') NOT ILIKE 'reserved'" //
						+ " ORDER BY _cm_read_comment(_cm_comment_for_attribute(A.cid, A.name), 'INDEX')::int", //
						new Object[] { entryTypeId }, new RowMapper<DBAttribute>() {
							@Override
							public DBAttribute mapRow(final ResultSet rs, final int rowNum) throws SQLException {
								final String name = rs.getString("name");
								final String comment = rs.getString("comment");
								final AttributeMetadata meta = attributeCommentToMetadata(comment);
								final CMAttributeType<?> type = SqlType.createAttributeType(rs.getString("sql_type"),
										meta);
								return new DBAttribute(name, type, meta);
							}
						});
		return entityTypeAttributes;
	}

	private static AttributeMetadata attributeCommentToMetadata(final String comment) {
		final AttributeMetadata meta = new AttributeMetadata();
		extractCommentToMetadata(comment, meta, Utils.ATTRIBUTE_COMMENT_MAPPER);
		return meta;
	}

	private enum InputOutput {
		i, o, io;
	}

	public List<DBFunction> findAllFunctions() {
		final List<DBFunction> functionList = jdbcTemplate.query("SELECT * FROM _cm_function_list()",
				new RowMapper<DBFunction>() {
					@Override
					public DBFunction mapRow(final ResultSet rs, final int rowNum) throws SQLException {
						final String name = rs.getString("function_name");
						final Long id = rs.getLong("function_id");
						final boolean returnsSet = rs.getBoolean("returns_set");
						final DBFunction function = new DBFunction(name, id, returnsSet);
						addParameters(rs, function);
						return function;
					}

					private void addParameters(final ResultSet rs, final DBFunction function) throws SQLException {
						final String[] argIo = (String[]) rs.getArray("arg_io").getArray();
						final String[] argNames = (String[]) rs.getArray("arg_names").getArray();
						final String[] argTypes = (String[]) rs.getArray("arg_types").getArray();
						if (argIo.length != argNames.length || argNames.length != argTypes.length) {
							return; // Can't happen
						}
						for (int i = 0; i < argIo.length; ++i) {
							final String name = argNames[i];
							final CMAttributeType<?> type = SqlType.createAttributeType(argTypes[i]);
							final InputOutput io = InputOutput.valueOf(argIo[i]);
							switch (io) {
							case i:
								function.addInputParameter(name, type);
								break;
							case o:
								function.addOutputParameter(name, type);
								break;
							case io:
								function.addInputParameter(name, type);
								function.addOutputParameter(name, type);
								break;
							}
						}
					}
				});
		return functionList;
	}

	/*
	 * Utils
	 */

	private static ClassMetadata classCommentToMetadata(final String comment) {
		final ClassMetadata meta = new ClassMetadata();
		extractCommentToMetadata(comment, meta, Utils.CLASS_COMMENT_MAPPER);
		return meta;
	}

	private static DomainMetadata domainCommentToMetadata(final String comment) {
		final DomainMetadata meta = new DomainMetadata();
		extractCommentToMetadata(comment, meta, Utils.DOMAIN_COMMENT_MAPPER);
		return meta;
	}

	private static void extractCommentToMetadata(final String comment, final EntryTypeMetadata meta,
			final CommentMapper mapper) {
		if (isNotBlank(comment)) {
			final Matcher commentMatcher = COMMENT_PATTERN.matcher(comment);
			while (commentMatcher.find()) {
				final String commentKey = commentMatcher.group(2);
				final String metaKey = mapper.getMetaNameFromComment(commentKey);
				if (metaKey != null) {
					final String commentValue = commentMatcher.group(3);
					final String metaValue = mapper.getMetaValueFromComment(commentKey, commentValue);
					meta.put(metaKey, metaValue);
				}
			}
		}
	}

}
