package org.cmdbuild.dao.backend.postgresql;

import static org.cmdbuild.utils.BinaryUtils.toByte;
import static org.cmdbuild.utils.StringUtils.arrayToCsv;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.dao.backend.SchemaCache;
import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.dao.backend.postgresql.SchemaQueries.AttributeQueries;
import org.cmdbuild.dao.backend.postgresql.SchemaQueries.DomainQueries;
import org.cmdbuild.dao.backend.postgresql.SchemaQueries.LookupQueries;
import org.cmdbuild.dao.backend.postgresql.SchemaQueries.TableQueries;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.AttributeImpl.AttributeDataDefinitionMeta;
import org.cmdbuild.elements.CardImpl;
import org.cmdbuild.elements.CardQueryImpl;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.DomainImpl;
import org.cmdbuild.elements.DomainImpl.DomainDataDefinitionMeta;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.elements.RelationImpl;
import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.elements.TableImpl.TableDataDefinitionMeta;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.DomainQuery;
import org.cmdbuild.elements.interfaces.IAbstractElement;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.model.Report;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.utils.StringUtils;
import org.cmdbuild.utils.tree.CNode;
import org.cmdbuild.utils.tree.CTree;

public class PGCMBackend extends CMBackend {

	protected final SchemaCache cache;

	public enum CMSqlException {
		CM_FORBIDDEN_OPERATION, CM_RESTRICT_VIOLATION {
			@Override
			public void throwException(final SQLException se) throws CMDBException {
				throw ORMExceptionType.ORM_CANT_DELETE_CARD_WITH_RELATION.createException();
			}
		},
		CM_CONTAINS_DATA {
			@Override
			public void throwException(final SQLException se) throws CMDBException {
				throw ORMExceptionType.ORM_CONTAINS_DATA.createException();
			}
		},
		CM_HAS_DOMAINS {
			@Override
			public void throwException(final SQLException se) throws CMDBException {
				throw ORMExceptionType.ORM_TABLE_HAS_DOMAIN.createException();
			}
		},
		CM_HAS_CHILDREN {
			@Override
			public void throwException(final SQLException se) throws CMDBException {
				throw ORMExceptionType.ORM_TABLE_HAS_CHILDREN.createException();
			}
		};

		public void throwException(final SQLException se) throws CMDBException {
			throwGenericException(se);
		}

		public static void throwCustomExceptionFrom(final SQLException se) throws CMDBException {
			try {
				fromSqlException(se).throwException(se);
			} catch (final IllegalArgumentException e) {
				throwGenericException(se);
			}
		}

		private static CMSqlException fromSqlException(final SQLException se) {
			return valueOf(extractSqlExceptionMessage(se));
		}

		private static String extractSqlExceptionMessage(final SQLException se) {
			final String message = se.getMessage();
			final String[] split = message.split("\\s", 3);
			return (split.length > 1) ? split[1] : message;
		}

		private static void throwGenericException(final SQLException se) throws CMDBException {
			throw ORMExceptionType.ORM_ERROR_GENERIC_SQL.createException(se.getMessage());
		}
	}

	public enum SqlState {
		not_null_violation("23502"), foreign_key_violation("23503"), unique_violation("23505") {
			@Override
			public void throwException(final SQLException se) throws CMDBException {
				throw ORMExceptionType.ORM_UNIQUE_VIOLATION.createException();
			}
		},
		duplicate_table("42P07") {
			@Override
			public void throwException(final SQLException se) throws CMDBException {
				throw ORMExceptionType.ORM_DUPLICATE_TABLE.createException();
			}
		},
		duplicate_column("42701") {
			@Override
			public void throwException(final SQLException se) throws CMDBException {
				// Cannot be thrown, because adding a new attribute
				// by the same name changes the existing attribute
				// (attributes have just a name, not an id!)
				throw ORMExceptionType.ORM_DUPLICATE_ATTRIBUTE.createException();
			}
		},
		raise_exception("P0001") {
			@Override
			public void throwException(final SQLException se) throws CMDBException {
				CMSqlException.throwCustomExceptionFrom(se);
			}
		};

		private final String errorCode;

		SqlState(final String errorCode) {
			this.errorCode = errorCode;
		}

		public String getErrorCode() {
			return errorCode;
		}

		public void throwException(final SQLException se) throws CMDBException {
			throw ORMExceptionType.ORM_ERROR_GENERIC_SQL.createException(se.getMessage());
		}

		public static void throwCustomExceptionFrom(final SQLException se) throws CMDBException {
			try {
				fromSqlException(se).throwException(se);
			} catch (final IllegalArgumentException e) {
				CMSqlException.throwCustomExceptionFrom(se);
			}
		}

		private static SqlState fromSqlException(final SQLException se) {
			final String sqlState = se.getSQLState();
			for (final SqlState state : values()) {
				if (state.getErrorCode().equals(sqlState)) {
					return state;
				}
			}
			throw new IllegalArgumentException("Invalid state");
		}
	}

	protected String COMMENT_REG_EXP = "(([A-Z0-9]+): ([^|]*))*";
	public static String COMMENT_SEPARATOR = "|";

	private static String[] ATTRIBUTE_META_IN_COMMENTS = { AttributeDataDefinitionMeta.MODE.toString(),
			AttributeDataDefinitionMeta.DESCR.toString(), AttributeDataDefinitionMeta.BASEDSP.toString(),
			AttributeDataDefinitionMeta.STATUS.toString(), AttributeDataDefinitionMeta.REFERENCEDOM.toString(),
			AttributeDataDefinitionMeta.GROUP.toString(), AttributeDataDefinitionMeta.REFERENCEDIRECT.toString(),
			AttributeDataDefinitionMeta.REFERENCETYPE.toString(), AttributeDataDefinitionMeta.LOOKUP.toString(),
			AttributeDataDefinitionMeta.FIELDMODE.toString(), AttributeDataDefinitionMeta.CLASSORDER.toString(),
			AttributeDataDefinitionMeta.FILTER.toString(), AttributeDataDefinitionMeta.INDEX.toString(),
			AttributeDataDefinitionMeta.FKTARGETCLASS.toString(), AttributeDataDefinitionMeta.EDITORTYPE.toString() };

	private static String[] TABLE_META_IN_COMMENTS = { TableDataDefinitionMeta.TYPE.toString(),
			TableDataDefinitionMeta.MODE.toString(), TableDataDefinitionMeta.DESCR.toString(),
			TableDataDefinitionMeta.STATUS.toString(), TableDataDefinitionMeta.SUPERCLASS.toString(),
			TableDataDefinitionMeta.USERSTOPPABLE.toString() };

	private static String[] DOMAIN_META_IN_COMMENTS = { DomainDataDefinitionMeta.TYPE.toString(),
			DomainDataDefinitionMeta.MODE.toString(), DomainDataDefinitionMeta.LABEL.toString(),
			DomainDataDefinitionMeta.STATUS.toString(), DomainDataDefinitionMeta.MASTERDETAIL.toString(),
			DomainDataDefinitionMeta.DESCRDIR.toString(), DomainDataDefinitionMeta.DESCRINV.toString(),
			DomainDataDefinitionMeta.CARDIN.toString(), DomainDataDefinitionMeta.OPENEDROWS.toString(),
			DomainDataDefinitionMeta.CLASS1.toString(), DomainDataDefinitionMeta.CLASS2.toString(),
			DomainDataDefinitionMeta.MDLABEL.toString() };

	private static Set<String> attributeMetaInComments;
	private static Set<String> tableMetaInComments;
	private static Set<String> domainMetaInComments;

	static {
		attributeMetaInComments = new HashSet<String>();
		for (final String metaName : ATTRIBUTE_META_IN_COMMENTS) {
			attributeMetaInComments.add(metaName);
		}
		tableMetaInComments = new HashSet<String>();
		for (final String metaName : TABLE_META_IN_COMMENTS) {
			tableMetaInComments.add(metaName);
		}
		domainMetaInComments = new HashSet<String>();
		for (final String metaName : DOMAIN_META_IN_COMMENTS) {
			domainMetaInComments.add(metaName);
		}
	}

	public PGCMBackend() {
		cache = new SchemaCache(this);
	}

	private String createComment(final BaseSchema schema) {
		final Map<String, String> dataDefinitionMeta = schema.genDataDefinitionMeta();
		final Set<String> metaInComments = getMetaInComments(schema);
		final StringBuffer comment = new StringBuffer();
		for (final Entry<String, String> meta : dataDefinitionMeta.entrySet()) {
			if (metaInComments.contains(meta.getKey())) {
				if (comment.length() > 0) {
					comment.append(COMMENT_SEPARATOR);
				}
				comment.append(meta.getKey()).append(": ").append(meta.getValue());
			}
		}
		return comment.toString();
	}

	private Set<String> getMetaInComments(final BaseSchema schema) {
		if (schema instanceof IAttribute) {
			return attributeMetaInComments;
		} else if (schema instanceof ITable) {
			return tableMetaInComments;
		} else if (schema instanceof IDomain) {
			return domainMetaInComments;
		} else {
			return new HashSet<String>();
		}
	}

	@Override
	public Map<String, String> parseComment(final String comment) {
		final Map<String, String> dataDefinitionMeta = new TreeMap<String, String>();
		if (comment != null && !comment.trim().isEmpty()) {
			final Pattern commentPattern = Pattern.compile(COMMENT_REG_EXP);
			final Matcher commentMatcher = commentPattern.matcher(comment);
			while (commentMatcher.find()) {
				final String key = commentMatcher.group(2);
				final String value = commentMatcher.group(3);
				if (isValid(key, value)) {
					dataDefinitionMeta.put(key, value);
				}
			}
		}
		return dataDefinitionMeta;
	}

	private boolean isValid(final String key, final String value) {
		return key != null && value != null && !value.trim().isEmpty();
	}

	/*
	 * Classes
	 */

	@Override
	public void deleteTable(final ITable table) throws ORMException {
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(TableQueries.DELETE.toString());
			stm.setInt(1, table.getId());
			stm.execute();
			cache.refreshTables();
		} catch (final SQLException se) {
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			DBService.close(null, stm, con);
		}
	}

	@Override
	public int createTable(final ITable table) throws ORMException {
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(TableQueries.CREATE.toString());
			stm.registerOutParameter(1, Types.INTEGER);
			stm.setString(2, table.getName());
			final ITable parent = table.getParent();
			if (parent != null) {
				stm.setString(3, table.getParent().getName());
			} else {
				stm.setString(3, null);
			}
			stm.setString(4, createComment(table));
			Log.SQL.debug(stm.toString());
			stm.execute();
			cache.refreshTables();
			return stm.getInt(1);
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors creating new table", se);
			SqlState.throwCustomExceptionFrom(se);
			return -1; // Never going to happen
		} finally {
			cache.refreshTables();
			DBService.close(null, stm, con);
		}
	}

	@Override
	public void modifyTable(final ITable table) throws ORMException {
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(TableQueries.MODIFY.toString());
			stm.setInt(1, table.getId());
			stm.setString(2, createComment(table));
			Log.SQL.debug(stm.toString());
			stm.execute();
			cache.refreshTables();
			cache.refreshDomains();
		} catch (final SQLException se) {
			Log.PERSISTENCE.info("Errors modifying table: " + table.getId(), se);
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			DBService.close(null, stm, con);
		}
	}

	@Override
	public Map<Integer, CNode<ITable>> loadTableMap() {
		final Map<Integer, CNode<ITable>> map = new HashMap<Integer, CNode<ITable>>();
		Statement stm = null;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = connection();
			stm = connection.createStatement();
			Log.SQL.debug(TableQueries.FIND_ALL.toString());
			rs = stm.executeQuery(TableQueries.FIND_ALL.toString());
			while (rs.next()) {
				final Integer classId = rs.getInt("classid");
				final String className = rs.getString("classname");
				final ITable table = new TableImpl(className, rs.getString("classcomment"), classId);
				Log.PERSISTENCE.debug(String.format("Table %s (%d) inserted into table map", table.getName(),
						table.getId()));
				map.put(classId, new CNode<ITable>(table));
			}
		} catch (final SQLException ex) {
			Log.PERSISTENCE.error("Errors retrieving all tables", ex);
		} finally {
			DBService.close(rs, stm, connection);
		}
		return map;
	}

	@Override
	public CTree<ITable> buildTableTree(final Map<Integer, CNode<ITable>> map) {
		final CTree<ITable> tree = new CTree<ITable>();
		CNode<ITable> rootNode = null;
		Statement stm = null;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = connection();
			stm = connection.createStatement();
			Log.SQL.debug(TableQueries.LOAD_TREE.toString());
			rs = stm.executeQuery(TableQueries.LOAD_TREE.toString());
			while (rs.next()) {
				final Integer parentId = rs.getInt("parentid");
				final Integer childId = rs.getInt("childid");
				if (!map.containsKey(parentId) || !map.containsKey(childId)) {
					Log.PERSISTENCE.warn("Can't find a suitable class for the tree!");
					continue;
				}
				final CNode<ITable> parentNode = map.get(parentId);
				final ITable parentTable = parentNode.getData();
				final CNode<ITable> childNode = map.get(childId);
				final ITable childTable = childNode.getData();
				childTable.setParent(parentTable);
				parentNode.addChild(childNode);
				if (rootNode == null && ITable.BaseTable.equals(parentTable.getName())) {
					rootNode = parentNode;
				}
				Log.PERSISTENCE.debug(String.format("Table %s (%d) is child of %s (%d)", childTable.getName(),
						childTable.getId(), parentTable.getName(), parentTable.getId()));
			}
		} catch (final SQLException ex) {
			Log.PERSISTENCE.error("Errors building table tree", ex);
		} finally {
			DBService.close(rs, stm, connection);
		}
		tree.setRootElement(rootNode);
		return tree;
	}

	/*
	 * Attributes
	 */

	@Override
	public void deleteAttribute(final IAttribute attribute) throws ORMException {
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(AttributeQueries.DELETE.toString());
			stm.setInt(1, attribute.getSchema().getId());
			stm.setString(2, attribute.getName());
			Log.SQL.debug(stm.toString());
			stm.execute();
			// TODO: remove the attribute from the table if actually removed
			cache.refreshTables();
			cache.refreshDomains();
		} catch (final SQLException se) {
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			DBService.close(null, stm, con);
		}
	}

	@Override
	public void createAttribute(final IAttribute attribute) throws ORMException {
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(AttributeQueries.CREATE.toString());
			stm.setInt(1, attribute.getSchema().getId());
			stm.setString(2, attribute.getName());
			stm.setString(3, createType(attribute));
			stm.setString(4, attribute.getDefaultValue());
			stm.setBoolean(5, attribute.isNotNull());
			stm.setBoolean(6, attribute.isUnique());
			stm.setString(7, createComment(attribute));
			Log.SQL.debug(stm.toString());
			stm.execute();

			// FIXME - if the attribute is a superclass, the children must be
			// reloaded too
			// at this moment we will be reload all tables
			final BaseSchema schema = attribute.getSchema();
			if (schema instanceof ITable && ((ITable) schema).isSuperClass()) {
				cache.refreshTables();
			} else {
				schema.addAttribute(attribute);
			}
			TemporaryObjectsBeforeSpringDI.getDriver().clearCache();
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors creating new attribute", se);
			SqlState.throwCustomExceptionFrom(se);
		} catch (final RuntimeException e) {
			cache.refreshTables();
			throw e;
		} finally {
			DBService.close(null, stm, con);
		}
	}

	@Override
	public void modifyAttribute(final IAttribute attribute) throws ORMException {
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(AttributeQueries.MODIFY.toString());
			stm.setInt(1, attribute.getSchema().getId());
			stm.setString(2, attribute.getName());
			stm.setString(3, createType(attribute));
			stm.setString(4, attribute.getDefaultValue());
			stm.setBoolean(5, attribute.isNotNull());
			stm.setBoolean(6, attribute.isUnique());
			stm.setString(7, createComment(attribute));
			Log.SQL.debug(stm.toString());
			stm.execute();

			final BaseSchema schema = attribute.getSchema();
			if (schema instanceof ITable && ((ITable) schema).isSuperClass()) {
				cache.refreshTables();
			}
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors creating new attribute", se);
			cache.refreshTables();
			SqlState.throwCustomExceptionFrom(se);
		} catch (final RuntimeException e) {
			cache.refreshTables();
			throw e;
		} finally {
			DBService.close(null, stm, con);
		}
	}

	private String createType(final IAttribute attribute) {
		final AttributeType type = attribute.getType();
		String typeString = type.toDBString();
		switch (type) {
		case CHAR:
			typeString += "(1)";
			break;
		case STRING:
			typeString += "(" + attribute.getLength() + ")";
			break;
		case DECIMAL:
			typeString += "(" + attribute.getPrecision() + "," + attribute.getScale() + ")";
			break;
		}
		return typeString;
	}

	@Override
	public Map<String, IAttribute> findAttributes(final BaseSchema schema) {
		final Map<String, IAttribute> list = new LinkedHashMap<String, IAttribute>();
		PreparedStatement stm = null;
		final Connection con = connection();
		ResultSet rs = null;
		try {
			stm = con.prepareStatement(AttributeQueries.FIND_ALL_BY_TABLE.toString());
			if (schema.getId() == 0)
				return list;
			stm.setInt(1, schema.getId());
			Log.SQL.debug(stm.toString());
			rs = stm.executeQuery();
			while (rs.next()) {
				try {
					final String attributeName = rs.getString("attributename");
					final String dbTypeName = rs.getString("attributetype");
					final Map<String, String> meta = metaFromResultSet(rs);
					final AttributeType type = computeType(dbTypeName, meta);
					if (type != null) {
						Log.PERSISTENCE.debug(String.format("Attribute %s.%s", schema.getName(), attributeName));
						final IAttribute attribute = AttributeImpl.create(schema, attributeName, type, meta);
						list.put(attributeName, attribute);
					} else {
						Log.PERSISTENCE.error(String.format("Attribute %s.%s: cannot compute type %s",
								schema.getName(), attributeName, dbTypeName));
					}
				} catch (final NotFoundException e) {
					Log.PERSISTENCE.error("Errors finding attributes in table: " + schema.getName(), e);
				} catch (final ORMException e) {
					if (Log.PERSISTENCE.isDebugEnabled()) {
						Log.PERSISTENCE.debug("Skipping attribute with wrong comment", e);
					} else {
						Log.PERSISTENCE.info("Skipping attribute with wrong comment");
					}
				}
			}
		} catch (final SQLException ex) {
			Log.PERSISTENCE.error("Errors finding attributes in table: " + schema.getName(), ex);
		} finally {
			DBService.close(rs, stm, con);
		}
		return list;
	}

	private Map<String, String> metaFromResultSet(final ResultSet rs) throws SQLException {
		final Map<String, String> meta = parseComment(rs.getString("attributecomment"));
		meta.put(AttributeDataDefinitionMeta.LENGTH.toString(), rs.getString("attributelength"));
		meta.put(AttributeDataDefinitionMeta.PRECISION.toString(), String.valueOf(rs.getInt("attributeprecision")));
		meta.put(AttributeDataDefinitionMeta.SCALE.toString(), String.valueOf(rs.getInt("attributescale")));
		meta.put(AttributeDataDefinitionMeta.NOTNULL.toString(), String.valueOf(rs.getBoolean("attributenotnull")));
		meta.put(AttributeDataDefinitionMeta.UNIQUE.toString(), String.valueOf(rs.getBoolean("isunique")));
		meta.put(AttributeDataDefinitionMeta.DEFAULT.toString(), rs.getString("attributedefault"));
		meta.put(AttributeDataDefinitionMeta.LOCAL.toString(), String.valueOf(rs.getBoolean("attributeislocal")));
		return meta;
	}

	private AttributeType computeType(final String dbTypeName, final Map<String, String> meta) {
		if (isReference(meta)) {
			return AttributeType.REFERENCE;
		} else if (isForeignKey(meta)) {
			return AttributeType.FOREIGNKEY;
		} else if (isLookup(meta)) {
			return AttributeType.LOOKUP;
		} else {
			return AttributeType.fromDBString(dbTypeName);
		}
	}

	private boolean isReference(final Map<String, String> meta) {
		return meta.containsKey(AttributeDataDefinitionMeta.REFERENCEDOM.toString());
	}

	private boolean isForeignKey(final Map<String, String> meta) {
		return meta.containsKey(AttributeDataDefinitionMeta.FKTARGETCLASS.toString());
	}

	private boolean isLookup(final Map<String, String> meta) {
		return meta.containsKey(AttributeDataDefinitionMeta.LOOKUP.toString());
	}

	/*
	 * Domains
	 */

	@Override
	public void modifyDomain(final IDomain domain) {
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(DomainQueries.MODIFY.toString());
			stm.setInt(1, domain.getId());
			stm.setString(2, createComment(domain));
			Log.SQL.debug(stm.toString());
			stm.execute();
			cache.refreshDomains();
		} catch (final SQLException ex) {
			Log.PERSISTENCE.error("Errors modifying domain: " + domain.getId(), ex);
			cache.refreshDomains();
			throw ORMExceptionType.ORM_ERROR_DOMAIN_MODIFY.createException();
		} catch (final RuntimeException re) {
			cache.refreshDomains();
			throw re;
		} finally {
			DBService.close(null, stm, con);
		}
	}

	@Override
	public int createDomain(final IDomain domain) {
		int id;
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(DomainQueries.CREATE.toString());
			stm.registerOutParameter(1, Types.INTEGER);
			stm.setString(2, domain.getName());
			stm.setString(3, createComment(domain));
			Log.SQL.debug(stm.toString());
			stm.execute();
			id = stm.getInt(1);
			cache.refreshDomains();
		} catch (final SQLException ex) {
			Log.PERSISTENCE.error("Errors creating new domain", ex);
			cache.refreshDomains();
			throw ORMExceptionType.ORM_ERROR_DOMAIN_CREATE.createException();
		} catch (final RuntimeException re) {
			cache.refreshDomains();
			throw re;
		} finally {
			DBService.close(null, stm, con);
		}
		return id;
	}

	@Override
	public void deleteDomain(final IDomain domain) {
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(DomainQueries.DELETE.toString());
			stm.setInt(1, domain.getId());
			Log.SQL.debug(stm.toString());
			stm.execute();
			cache.refreshDomains();
		} catch (final SQLException ex) {
			Log.PERSISTENCE.error("Errors deleting domain", ex);
			throw ORMExceptionType.ORM_ERROR_DOMAIN_DELETE.createException();
		} finally {
			DBService.close(null, stm, con);
		}
	}

	@Override
	public Iterator<IDomain> getDomainList(final DomainQuery query) {
		final List<IDomain> list = new LinkedList<IDomain>();
		PreparedStatement stm = null;
		final Connection con = connection();
		ResultSet rs = null;
		try {
			if (query.isInherited()) {
				final Collection<String> ancestortree = TableImpl.tree().path(query.getTableName());
				final String tablePath = StringUtils.join(ancestortree, ",");
				stm = con.prepareStatement(DomainQueries.FIND_ALL_INHERITED_BY_TABLE.toString());
				stm.setString(1, tablePath);
				stm.setString(2, tablePath);
			} else {
				stm = con.prepareStatement(DomainQueries.FIND_ALL_BY_TABLE.toString());
				stm.setString(1, query.getTableName());
				stm.setString(2, query.getTableName());
			}
			Log.SQL.debug(stm.toString());
			rs = stm.executeQuery();
			while (rs.next()) {
				try {
					final IDomain domain = cache.getDomain(rs.getInt("domainid"));
					list.add(domain);
				} catch (final NotFoundException e) {
					Log.PERSISTENCE.debug("Domain table not found", e);
				}
			}
		} catch (final SQLException ex) {
			Log.PERSISTENCE.error("Errors retrieving domains for class", ex);
		} catch (final NotFoundException ex) {
			Log.PERSISTENCE.error("Errors retrieving class tree for retrieving hyerarchical domains", ex);
		} finally {
			DBService.close(rs, stm, con);
		}
		return list.iterator();
	}

	@Override
	public Iterable<IDomain> getDomainList() {
		return cache.getDomainList();
	}

	@Override
	public Map<Integer, IDomain> loadDomainMap() {
		final Map<Integer, IDomain> map = new HashMap<Integer, IDomain>();
		Statement stm = null;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = connection();
			stm = connection.createStatement();
			rs = stm.executeQuery(DomainQueries.FIND_ALL.toString());
			while (rs.next()) {
				final Integer domainId = rs.getInt("domainid");
				final String domainName = rs.getString("domainname");
				final String domainComment = rs.getString("domaincomment");
				Log.PERSISTENCE.debug(String.format("Domain %s (%d) inserted into domain map", domainName, domainId));
				try {
					map.put(domainId, new DomainImpl(domainName, domainComment, domainId));
				} catch (final CMDBException e) {
					Log.PERSISTENCE.error("Unable to add domain " + domainName);
				}
			}
		} catch (final SQLException ex) {
			Log.PERSISTENCE.error("Errors retrieving all domains", ex);
		} finally {
			DBService.close(rs, stm, connection);
		}
		return map;
	}

	/*
	 * Lookup Types
	 */

	@Override
	public void createLookupType(final LookupType lookupType) {
		final String type = lookupType.getType();
		String parentType = lookupType.getParentTypeName();

		// can't create a lookup type with empty name
		if ((null == type) || "".equals(type)) {
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
		if ("".equals(parentType)) {
			parentType = null;
		}
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(LookupQueries.CREATE_LOOKUPTYPE.toString());
			stm.setString(1, type);
			stm.setString(2, parentType);
			Log.SQL.debug(stm.toString());
			stm.execute();
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors creating new lookup type", se);
			// TODO handle existing lookups
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			cache.refreshLookups();
			DBService.close(null, stm, con);
		}
	}

	@Override
	public void modifyLookupType(final LookupType lookupType) {
		final String type = lookupType.getType();
		final String savedType = lookupType.getSavedType();
		// can't create a lookup type with empty name
		if ((null == type) || "".equals(type)) {
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(LookupQueries.MODIFY_LOOKUPTYPE.toString());
			stm.setString(1, type);
			stm.setString(2, savedType);
			stm.setString(3, type);
			stm.setString(4, savedType);
			Log.SQL.debug(stm.toString());
			stm.execute();
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors updating lookup type", se);
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			cache.refreshLookups();
			DBService.close(null, stm, con);
		}
	}

	@Override
	public void deleteLookupType(final LookupType lookupType) {
		CallableStatement stm = null;
		final Connection con = connection();
		try {
			stm = con.prepareCall(LookupQueries.DELETE_LOOKUPTYPE.toString());
			stm.setString(1, lookupType.getSavedType());
			Log.SQL.debug(stm.toString());
			stm.execute();
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors deleting lookup type", se);
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			cache.refreshLookups();
			DBService.close(null, stm, con);
		}
	}

	@Override
	public CTree<LookupType> loadLookupTypeTree() {
		Statement stm = null;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = connection();
			stm = connection.createStatement();
			final String query = LookupQueries.LOAD_TREE_TYPES.toString();
			Log.SQL.debug(query);
			rs = stm.executeQuery(query);
			return buildTree(rs);
		} catch (final Exception ex) {
			Log.CMDBUILD.error("Errors retrieving lookup tree", ex);
		} finally {
			DBService.close(rs, stm, connection);
		}
		return new CTree<LookupType>();
	}

	private CTree<LookupType> buildTree(final ResultSet rs) throws SQLException {
		// prepare hash
		final HashMap<String, CNode<LookupType>> tempHash = new HashMap<String, CNode<LookupType>>();
		final LookupType root = LookupType.createFromDB("root", null);
		while (rs.next()) {
			final String type = rs.getString("Type");
			final String parentType = rs.getString("ParentType");
			final CNode<LookupType> node = new CNode<LookupType>();
			final LookupType lookupType = LookupType.createFromDB(type, parentType);
			node.setData(lookupType);
			tempHash.put(lookupType.getType(), node);
		}

		// prepare tree
		final CNode<LookupType> rootNode = new CNode<LookupType>();
		rootNode.setData(root);
		final CTree<LookupType> tree = new CTree<LookupType>();
		tree.setRootElement(rootNode);

		for (final CNode<LookupType> childNode : tempHash.values()) {
			final CNode<LookupType> parentNode = tempHash.get(childNode.getData().getParentTypeName());
			if (parentNode == null)
				rootNode.addChild(childNode);
			else
				parentNode.addChild(childNode);
		}
		return tree;
	}

	/*
	 * Lookup
	 */

	@Override
	public void modifyLookup(final Lookup lookup) {
		Statement stm = null;
		final Connection con = connection();
		try {
			final LookupQueryBuilder qb = new LookupQueryBuilder();
			stm = con.createStatement();
			final String query = qb.buildUpdateQuery(lookup);
			if (!query.equals("")) {
				Log.SQL.debug(query);
				stm.executeUpdate(query);
			}
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors modifying lookup", se);
			throw ORMExceptionType.ORM_ERROR_LOOKUP_MODIFY.createException();
		} finally {
			DBService.close(null, stm, con);
		}
	}

	@Override
	public int createLookup(final Lookup lookup) {
		PreparedStatement stm = null;
		final Connection con = connection();
		ResultSet rs = null;
		try {
			final LookupQueryBuilder qb = new LookupQueryBuilder();
			final String query = qb.buildInsertQuery(lookup);
			stm = con.prepareStatement(query);
			Log.SQL.debug(query);
			stm.executeQuery();
			rs = stm.getResultSet();
			if (!rs.next()) {
				Log.PERSISTENCE.error("Error retrieving generated primary key");
				throw ORMExceptionType.ORM_ERROR_GETTING_PK.createException();
			}
			return rs.getInt(1);
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors creating lookup", se);
			throw ORMExceptionType.ORM_ERROR_LOOKUP_CREATE.createException();
		} finally {
			cache.refreshLookups();
			DBService.close(null, stm, con);
		}
	}

	@Override
	public List<Lookup> findLookups() {
		final List<Lookup> list = new LinkedList<Lookup>();
		Statement stm = null;
		final Connection con = connection();
		ResultSet rs = null;
		try {
			final ITable lookupTable = UserOperations.from(UserContext.systemContext()).tables().get("LookUp");
			final Collection<IAttribute> attributes = lookupTable.getAttributes().values();
			final LookupQueryBuilder qb = new LookupQueryBuilder();
			final String query = qb.buildSelectQuery();
			final Map<String, QueryAttributeDescriptor> queryMapping = qb.getQueryComponents().getQueryMapping();
			stm = con.createStatement();
			Log.SQL.debug(query);
			rs = stm.executeQuery(query);
			while (rs.next()) {
				final Lookup lookup = new Lookup();
				for (final IAttribute attribute : attributes) {
					lookup.setValue(attribute.getName(), rs, queryMapping.get(attribute.getName()));
				}
				lookup.resetAttributes();
				list.add(lookup);
			}
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors finding cards", se);
		} catch (final NotFoundException e) {
			return null;
		} finally {
			DBService.close(rs, stm, con);
		}
		return list;
	}

	/*
	 * Relation
	 */

	@Override
	public int createRelation(final IRelation relation) {
		int id;
		PreparedStatement stm = null;
		final Connection con = connection();
		ResultSet rs = null;
		try {
			final RelationQueryBuilder qb = new RelationQueryBuilder();
			final String query = qb.buildInsertQuery(relation);
			stm = con.prepareStatement(query);
			stm.executeQuery();
			rs = stm.getResultSet();
			if (rs.next()) {
				id = rs.getInt(1);
			} else {
				Log.PERSISTENCE.error("Error retrieving generated primary key");
				throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
			}
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors creating relation", se);
			throw ORMExceptionType.ORM_ERROR_RELATION_CREATE.createException();
		} finally {
			DBService.close(null, stm, con);
		}
		return id;
	}

	@Override
	public void modifyRelation(final IRelation relation) {
		Statement stm = null;
		final Connection con = connection();
		try {
			final RelationQueryBuilder qb = new RelationQueryBuilder();
			final String query = qb.buildUpdateQuery(relation);
			stm = con.createStatement();
			stm.executeUpdate(query);
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors modifying relation", se);
			throw ORMExceptionType.ORM_ERROR_RELATION_MODIFY.createException();
		} finally {
			DBService.close(null, stm, con);
		}
	}

	private static List<IRelation> perfomRelationQuery(final IDomain domain, final String query,
			final QueryComponents queryComponents) {
		final List<IRelation> list = new LinkedList<IRelation>();
		Statement stm = null;
		final Connection con = connection();
		ResultSet rs = null;
		try {
			stm = con.createStatement();
			Log.SQL.debug(query);
			rs = stm.executeQuery(query);
			final Map<String, QueryAttributeDescriptor> attrMapping1 = queryComponents.getQueryMapping("Table1");
			final Map<String, QueryAttributeDescriptor> attrMapping2 = queryComponents.getQueryMapping("Table2");
			final Map<String, QueryAttributeDescriptor> attrMappingM = queryComponents.getQueryMapping("Map");
			while (rs.next()) {
				try {
					final CardImpl card1 = new CardImpl(rs.getInt(attrMappingM.get(
							IRelation.RelationAttributes.IdClass1.toString()).getValueAlias()));
					card1.setValue(ICard.CardAttributes.Id.toString(), rs,
							attrMappingM.get(IRelation.RelationAttributes.IdObj1.toString()));
					final CardImpl card2 = new CardImpl(rs.getInt(attrMappingM.get(
							IRelation.RelationAttributes.IdClass2.toString()).getValueAlias()));
					card2.setValue(ICard.CardAttributes.Id.toString(), rs,
							attrMappingM.get(IRelation.RelationAttributes.IdObj2.toString()));
					for (final String attrName : attrMapping1.keySet()) {
						card1.setValue(attrName, rs, attrMapping1.get(attrName));
					}
					for (final String attrName : attrMapping2.keySet()) {
						card2.setValue(attrName, rs, attrMapping2.get(attrName));
					}
					final IRelation relation = new RelationImpl(domain, card1, card2);
					for (final String attrName : attrMappingM.keySet()) {
						relation.setValue(attrName, rs, attrMappingM.get(attrName));
					}
					relation.resetAttributes();
					list.add(relation);
				} catch (final NotFoundException e) {
					Log.PERSISTENCE.debug("card in relation not found", e);
				}
			}
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors finding relations", se);
		} finally {
			DBService.close(rs, stm, con);
		}
		return list;
	}

	@Override
	public IRelation getRelation(final IDomain domain, final ICard card1, final ICard card2) {
		final RelationQueryBuilder qb = new RelationQueryBuilder();
		final String query = qb.buildSelectQuery(domain, card1.getId(), card2.getId());
		final Iterator<IRelation> relationIterator = perfomRelationQuery(domain, query, qb.getQueryComponents())
				.iterator();
		if (!relationIterator.hasNext()) {
			throw NotFoundExceptionType.NOTFOUND.createException();
		}
		return relationIterator.next();
	}

	@Override
	public IRelation getRelation(final IDomain domain, final int id) {
		final RelationQueryBuilder qb = new RelationQueryBuilder();
		final String query = qb.buildSelectQuery(domain, id);
		final Iterator<IRelation> relationIterator = perfomRelationQuery(domain, query, qb.getQueryComponents())
				.iterator();
		if (!relationIterator.hasNext()) {
			throw NotFoundExceptionType.NOTFOUND.createException();
		}
		return relationIterator.next();
	}

	@Override
	public Iterable<IRelation> getRelationList(final DirectedDomain directedDomain, final int sourceId) {
		final IDomain domain = directedDomain.getDomain();
		final RelationQueryBuilder qb = new RelationQueryBuilder();
		final String query;
		if (directedDomain.getDirectionValue()) {
			query = qb.buildSelectQuery(domain, sourceId, 0);
		} else {
			query = qb.buildSelectQuery(domain, 0, sourceId);
		}
		return perfomRelationQuery(domain, query, qb.getQueryComponents());
	}

	/*
	 * Card
	 */

	@Override
	public int createCard(final ICard card) {
		int id;
		PreparedStatement stm = null;
		final Connection con = connection();
		ResultSet rs = null;
		try {
			final CardQueryBuilder qb = new CardQueryBuilder();
			final String query = qb.buildInsertQuery(card);
			stm = con.prepareStatement(query);
			stm.executeQuery();
			rs = stm.getResultSet();
			if (rs.next()) {
				id = rs.getInt(1);
			} else {
				Log.PERSISTENCE
						.error("Error retrieving generated primary key: is there a trigger ignoring the insert?");
				id = 0;
			}
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors creating card", se);
			SqlState.throwCustomExceptionFrom(se);
			return -1; // Never going to happen
		} finally {
			DBService.close(null, stm, con);
		}
		return id;
	}

	@Override
	public void modifyCard(final ICard card) {
		Statement stm = null;
		final Connection con = connection();
		try {
			final CardQueryBuilder qb = new CardQueryBuilder();
			final String query = qb.buildUpdateQuery(card);
			stm = con.createStatement();
			stm.executeUpdate(query);
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors modifying card", se);
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			DBService.close(null, stm, con);
		}
	}

	@Override
	public List<ICard> getCardList(final CardQueryImpl cardQuery) {
		final List<ICard> list = new LinkedList<ICard>();
		Statement stm = null;
		final Connection con = connection();
		ResultSet rs = null;
		try {
			final CardQueryBuilder qb = new CardQueryBuilder();
			final String query = cardQueryToSQL(cardQuery, qb);
			final Map<String, QueryAttributeDescriptor> queryMapping = qb.getQueryComponents().getQueryMapping();
			stm = con.createStatement();
			rs = stm.executeQuery(query);

			int totalRows = 0;
			final boolean countQuery = cardQuery.needsCount();
			final boolean historyQuery = cardQuery.isHistory();
			final ITable table = cardQuery.getTable();
			final Set<String> attributes = cardQuery.getAttributes();

			while (rs.next()) {
				if (totalRows == 0 && countQuery)
					totalRows = rs.getInt("Count");
				ICard card;
				// to prevent inexistent tables to be requested (superclass
				// check for forged Tables like Menu)
				if (!table.isSuperClass() || historyQuery) {
					card = new CardImpl(table);
				} else {
					final int classId = rs.getInt(queryMapping.get(ICard.CardAttributes.ClassId.toString())
							.getValueAlias());
					card = new CardImpl(classId);
				}
				for (final String attributeName : attributes) {
					try {
						card.setValue(attributeName, rs, queryMapping.get(attributeName));
					} catch (final NotFoundException e) {
						Log.SQL.error(String.format("Inexistent attribute \"%s\" for table \"%s\"", attributeName,
								table.getName()));
					}
				}
				card.resetAttributes();
				list.add(card);
			}
			cardQuery.setTotalRows(totalRows);
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors finding cards", se);
			throw ORMExceptionType.ORM_ERROR_CARD_SELECT.createException();
		} finally {
			DBService.close(rs, stm, con);
		}
		return list;
	}

	@Override
	public String cardQueryToSQL(final CardQuery cardQuery, final CardQueryBuilder qb) {
		final Set<String> attributes = cardQuery.getAttributes();
		// Automatically add all table attributes to the query if not specified
		// otherwise
		if (attributes.isEmpty()) {
			attributes.addAll(cardQuery.getTable().getAttributes().keySet());
		}
		// Add ClassId because it is needed to create the card
		if (!attributes.contains(ICard.CardAttributes.ClassId.toString())) {
			attributes.add(ICard.CardAttributes.ClassId.toString());
		}
		return qb.buildSelectQuery(cardQuery);
	}

	@Override
	public int getCardPosition(final CardQuery query, final int cardId) {
		int position;
		// Automatically add all table attributes to the query if not specified
		// otherwise
		if (query.getAttributes().isEmpty()) {
			query.attributes(query.getTable().getAttributes().keySet().toArray(new String[0]));
		}
		Statement stm = null;
		final Connection con = connection();
		ResultSet rs = null;
		try {
			final CardQueryBuilder qb = new CardQueryBuilder();
			stm = con.createStatement();
			stm.executeQuery(CardQueryBuilder.CARD_ZERO_INDEX);
			rs = stm.executeQuery(qb.buildPositionQuery(query, cardId));
			if (rs.next()) {
				position = rs.getInt(1);
			} else {
				position = -1;
			}
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors getting card position", se);
			throw NotFoundExceptionType.CARD_NOTFOUND.createException(query.getTable().toString());
		} finally {
			DBService.close(rs, stm, con);
		}
		return position;
	}

	@Override
	public void updateCardsFromTemplate(final CardQuery cardQuery, final ICard cardTemplate) {
		Statement stm = null;
		final Connection con = connection();
		final ResultSet rs = null;
		try {
			final CardQueryBuilder qb = new CardQueryBuilder();
			final String query = qb.buildUpdateQuery(cardQuery, cardTemplate);
			stm = con.createStatement();
			stm.executeUpdate(query);
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors updating cards from template", se);
			throw ORMExceptionType.ORM_ERROR_CARD_UPDATE.createException();
		} finally {
			DBService.close(rs, stm, con);
		}
	}

	@Override
	public void deleteElement(final IAbstractElement element) {
		Statement stm = null;
		final Connection con = connection();
		final ResultSet rs = null;
		try {
			final String query = String.format("DELETE FROM \"%s\" WHERE \"Id\"=%d", element.getSchema().getDBName(),
					element.getId());
			Log.SQL.debug(query);
			stm = con.createStatement();
			stm.executeUpdate(query);
		} catch (final SQLException se) {
			Log.PERSISTENCE.error("Errors deleting card", se);
			throw ORMExceptionType.ORM_ERROR_CARD_UPDATE.createException();
		} finally {
			DBService.close(rs, stm, con);
		}
	}

	private static Connection connection() {
		return DBService.getConnection();
	}

	/*
	 * Wrapper for Schema Cache
	 */

	@Override
	public ITable getTable(final String tableName) {
		return cache.getTable(tableName);
	}

	@Override
	public ITable getTable(final Integer classId) {
		return cache.getTable(classId);
	}

	@Override
	public IDomain getDomain(final String domainName) {
		return cache.getDomain(domainName);
	}

	@Override
	public IDomain getDomain(final Integer domainId) {
		return cache.getDomain(domainId);
	}

	@Override
	public Lookup getLookup(final Integer lookupId) {
		return cache.getLookup(lookupId);
	}

	@Override
	public Lookup getLookup(final String type, final String description) {
		return cache.getLookup(type, description);
	}

	@Override
	public Lookup getFirstLookupByCode(final String type, final String code) {
		return cache.getFirstLookupByCode(type, code);
	}

	@Override
	public List<Lookup> getLookupList(final String type, final String description) {
		return cache.getLookupList(type, description);
	}

	@Override
	public Iterable<LookupType> getLookupTypeList() {
		return cache.getLookupTypeList();
	}

	@Override
	public CTree<LookupType> getLookupTypeTree() {
		return cache.getLookupTypeTree();
	}

	@Override
	public LookupType getLookupType(final String type) {
		return cache.getLookupType(type);
	}

	@Override
	public LookupType getLookupTypeOrDie(final String type) {
		final LookupType lt = getLookupType(type);
		if (lt == null) {
			throw NotFoundExceptionType.LOOKUP_NOTFOUND.createException(type);
		}
		return lt;
	}

	@Override
	public Iterable<ITable> getTableList() {
		return cache.getTableList();
	}

	@Override
	public TableTree getTableTree() {
		return cache.getTableTree();
	}

	/*
	 * FIXME
	 */

	@Override
	public void clearCache() {
		cache.refreshTables();
		cache.refreshDomains();
		cache.refreshLookups();
	}
}
