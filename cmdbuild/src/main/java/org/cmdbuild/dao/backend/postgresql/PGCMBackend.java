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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.dao.backend.SchemaCache;
import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.dao.backend.postgresql.ReportQueryBuilder.ReportQueries;
import org.cmdbuild.dao.backend.postgresql.SchemaQueries.AttributeQueries;
import org.cmdbuild.dao.backend.postgresql.SchemaQueries.DomainQueries;
import org.cmdbuild.dao.backend.postgresql.SchemaQueries.LookupQueries;
import org.cmdbuild.dao.backend.postgresql.SchemaQueries.TableQueries;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.CardImpl;
import org.cmdbuild.elements.CardQueryImpl;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.DomainImpl;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.elements.RelationImpl;
import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.AttributeImpl.AttributeDataDefinitionMeta;
import org.cmdbuild.elements.DomainImpl.DomainDataDefinitionMeta;
import org.cmdbuild.elements.TableImpl.TableDataDefinitionMeta;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.DomainQuery;
import org.cmdbuild.elements.interfaces.IAbstractElement;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.StringUtils;
import org.cmdbuild.utils.tree.CNode;
import org.cmdbuild.utils.tree.CTree;

public class PGCMBackend extends CMBackend {

	protected final SchemaCache cache;

	public enum CMSqlException {
		CM_FORBIDDEN_OPERATION, CM_RESTRICT_VIOLATION {
			public void throwException(final SQLException se) throws CMDBException {
				throw ORMExceptionType.ORM_CANT_DELETE_CARD_WITH_RELATION.createException();
			}
		},
		CM_CONTAINS_DATA {
			public void throwException(final SQLException se) throws CMDBException {
				throw ORMExceptionType.ORM_CONTAINS_DATA.createException();
			}
		},
		CM_HAS_DOMAINS {
			public void throwException(final SQLException se) throws CMDBException {
				throw ORMExceptionType.ORM_TABLE_HAS_DOMAIN.createException();
			}
		},
		CM_HAS_CHILDREN {
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
			} catch (IllegalArgumentException e) {
				throwGenericException(se);
			}
		}

		private static CMSqlException fromSqlException(final SQLException se) {
			return valueOf(extractSqlExceptionMessage(se));
		}

		private static String extractSqlExceptionMessage(SQLException se) {
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
			public void throwException(SQLException se) throws CMDBException {
				throw ORMExceptionType.ORM_UNIQUE_VIOLATION.createException();
			}
		},
		duplicate_table("42P07") {
			public void throwException(SQLException se) throws CMDBException {
				throw ORMExceptionType.ORM_DUPLICATE_TABLE.createException();
			}
		},
		duplicate_column("42701") {
			public void throwException(SQLException se) throws CMDBException {
				// Cannot be thrown, because adding a new attribute
				// by the same name changes the existing attribute
				// (attributes have just a name, not an id!)
				throw ORMExceptionType.ORM_DUPLICATE_ATTRIBUTE.createException();
			}
		},
		raise_exception("P0001") {
			public void throwException(SQLException se) throws CMDBException {
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
			} catch (IllegalArgumentException e) {
				CMSqlException.throwCustomExceptionFrom(se);
			}
		}

		private static SqlState fromSqlException(final SQLException se) {
			final String sqlState = se.getSQLState();
			for (SqlState state : values()) {
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
			AttributeDataDefinitionMeta.FKTARGETCLASS.toString(), AttributeDataDefinitionMeta.EDITORTYPE.toString()};

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
		for (String metaName : ATTRIBUTE_META_IN_COMMENTS) {
			attributeMetaInComments.add(metaName);
		}
		tableMetaInComments = new HashSet<String>();
		for (String metaName : TABLE_META_IN_COMMENTS) {
			tableMetaInComments.add(metaName);
		}
		domainMetaInComments = new HashSet<String>();
		for (String metaName : DOMAIN_META_IN_COMMENTS) {
			domainMetaInComments.add(metaName);
		}
	}

	public PGCMBackend() {
		cache = new SchemaCache(this);
	}

	private String createComment(BaseSchema schema) {
		Map<String, String> dataDefinitionMeta = schema.genDataDefinitionMeta();
		Set<String> metaInComments = getMetaInComments(schema);
		StringBuffer comment = new StringBuffer();
		for (Entry<String, String> meta : dataDefinitionMeta.entrySet()) {
			if (metaInComments.contains(meta.getKey())) {
				if (comment.length() > 0) {
					comment.append(COMMENT_SEPARATOR);
				}
				comment.append(meta.getKey()).append(": ").append(meta.getValue());
			}
		}
		return comment.toString();
	}

	private Set<String> getMetaInComments(BaseSchema schema) {
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
	public Map<String, String> parseComment(String comment) {
		Map<String, String> dataDefinitionMeta = new TreeMap<String, String>();
		if (comment != null && !comment.trim().isEmpty()) {
			Pattern commentPattern = Pattern.compile(COMMENT_REG_EXP);
			Matcher commentMatcher = commentPattern.matcher(comment);
			while (commentMatcher.find()) {
				String key = commentMatcher.group(2);
				String value = commentMatcher.group(3);
				if (isValid(key, value)) {
					dataDefinitionMeta.put(key, value);
				}
			}
		}
		return dataDefinitionMeta;
	}

	private boolean isValid(String key, String value) {
		return key != null && value != null && !value.trim().isEmpty();
	}

	/*
	 * Classes
	 */

	@Override
	public void deleteTable(ITable table) throws ORMException {
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
		try {
			stm = con.prepareCall(TableQueries.DELETE.toString());
			stm.setInt(1, table.getId());
			stm.execute();
			cache.refreshTables();
		} catch (SQLException se) {
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			DBService.close(null, stm);
		}
	}

	@Override
	public int createTable(ITable table) throws ORMException {
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
		try {
			stm = con.prepareCall(TableQueries.CREATE.toString());
			stm.registerOutParameter(1, Types.INTEGER);
			stm.setString(2, table.getName());
			ITable parent = table.getParent();
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
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors creating new table", se);
			SqlState.throwCustomExceptionFrom(se);
			return -1; // Never going to happen
		} finally {
			cache.refreshTables();
			DBService.close(null, stm);
		}
	}

	@Override
	public void modifyTable(ITable table) throws ORMException {
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
		try {
			stm = con.prepareCall(TableQueries.MODIFY.toString());
			stm.setInt(1, table.getId());
			stm.setString(2, createComment(table));
			Log.SQL.debug(stm.toString());
			stm.execute();
			cache.refreshTables();
			cache.refreshDomains();
		} catch (SQLException se) {
			Log.PERSISTENCE.info("Errors modifying table: " + table.getId(), se);
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			DBService.close(null, stm);
		}
	}

	@Override
	public Map<Integer, CNode<ITable>> loadTableMap() {
		Map<Integer, CNode<ITable>> map = new HashMap<Integer, CNode<ITable>>();
		Statement stm = null;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DBService.getConnection();
			stm = connection.createStatement();
			Log.SQL.debug(TableQueries.FIND_ALL.toString());
			rs = stm.executeQuery(TableQueries.FIND_ALL.toString());
			while (rs.next()) {
				Integer classId = rs.getInt("classid");
				String className = rs.getString("classname");
				ITable table = new TableImpl(className, rs.getString("classcomment"), classId);
				Log.PERSISTENCE.debug(String.format("Table %s (%d) inserted into table map", table.getName(),
						table.getId()));
				map.put(classId, new CNode<ITable>(table));
			}
		} catch (SQLException ex) {
			Log.PERSISTENCE.error("Errors retrieving all tables", ex);
		} finally {
			DBService.close(rs, stm);
		}
		return map;
	}

	@Override
	public CTree<ITable> buildTableTree(Map<Integer, CNode<ITable>> map) {
		CTree<ITable> tree = new CTree<ITable>();
		CNode<ITable> rootNode = null;
		Statement stm = null;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DBService.getConnection();
			stm = connection.createStatement();
			Log.SQL.debug(TableQueries.LOAD_TREE.toString());
			rs = stm.executeQuery(TableQueries.LOAD_TREE.toString());
			while (rs.next()) {
				Integer parentId = rs.getInt("parentid");
				Integer childId = rs.getInt("childid");
				if (!map.containsKey(parentId) || !map.containsKey(childId)) {
					Log.PERSISTENCE.warn("Can't find a suitable class for the tree!");
					continue;
				}
				CNode<ITable> parentNode = map.get(parentId);
				ITable parentTable = parentNode.getData();
				CNode<ITable> childNode = map.get(childId);
				ITable childTable = childNode.getData();
				childTable.setParent(parentTable);
				parentNode.addChild(childNode);
				if (rootNode == null && ITable.BaseTable.equals(parentTable.getName())) {
					rootNode = parentNode;
				}
				Log.PERSISTENCE.debug(String.format("Table %s (%d) is child of %s (%d)", childTable.getName(),
						childTable.getId(), parentTable.getName(), parentTable.getId()));
			}
		} catch (SQLException ex) {
			Log.PERSISTENCE.error("Errors building table tree", ex);
		} finally {
			DBService.close(rs, stm);
		}
		tree.setRootElement(rootNode);
		return tree;
	}

	/*
	 * Attributes
	 */

	@Override
	public void deleteAttribute(IAttribute attribute) throws ORMException {
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
		try {
			stm = con.prepareCall(AttributeQueries.DELETE.toString());
			stm.setInt(1, attribute.getSchema().getId());
			stm.setString(2, attribute.getName());
			Log.SQL.debug(stm.toString());
			stm.execute();
			// TODO: remove the attribute from the table if actually removed
			cache.refreshTables();
			cache.refreshDomains();
		} catch (SQLException se) {
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			DBService.close(null, stm);
		}
	}

	@Override
	public void createAttribute(IAttribute attribute) throws ORMException {
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
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
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors creating new attribute", se);
			SqlState.throwCustomExceptionFrom(se);
		} catch (RuntimeException e) {
			cache.refreshTables();
			throw e;
		} finally {
			DBService.close(null, stm);
		}
	}

	@Override
	public void modifyAttribute(IAttribute attribute) throws ORMException {
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
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
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors creating new attribute", se);
			cache.refreshTables();
			SqlState.throwCustomExceptionFrom(se);
		} catch (RuntimeException e) {
			cache.refreshTables();
			throw e;
		} finally {
			DBService.close(null, stm);
		}
	}

	private String createType(IAttribute attribute) {
		AttributeType type = attribute.getType();
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
	public Map<String, IAttribute> findAttributes(BaseSchema schema) {
		Map<String, IAttribute> list = new LinkedHashMap<String, IAttribute>();
		PreparedStatement stm = null;
		Connection con = DBService.getConnection();
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
					String attributeName = rs.getString("attributename");
					String dbTypeName = rs.getString("attributetype");
					Map<String, String> meta = metaFromResultSet(rs);
					AttributeType type = computeType(dbTypeName, meta);
					if (type != null) {
						Log.PERSISTENCE.debug(String.format("Attribute %s.%s", schema.getName(), attributeName));
						IAttribute attribute = AttributeImpl.create(schema, attributeName, type, meta);
						list.put(attributeName, attribute);
					} else {
						Log.PERSISTENCE.error(String.format("Attribute %s.%s: cannot compute type %s",
								schema.getName(), attributeName, dbTypeName));
					}
				} catch (NotFoundException e) {
					Log.PERSISTENCE.error("Errors finding attributes in table: " + schema.getName(), e);
				} catch (ORMException e) {
					if (Log.PERSISTENCE.isDebugEnabled()) {
						Log.PERSISTENCE.debug("Skipping attribute with wrong comment", e);
					} else {
						Log.PERSISTENCE.info("Skipping attribute with wrong comment");
					}
				}
			}
		} catch (SQLException ex) {
			Log.PERSISTENCE.error("Errors finding attributes in table: " + schema.getName(), ex);
		} finally {
			DBService.close(rs, stm);
		}
		return list;
	}

	private Map<String, String> metaFromResultSet(ResultSet rs) throws SQLException {
		Map<String, String> meta = parseComment(rs.getString("attributecomment"));
		meta.put(AttributeDataDefinitionMeta.LENGTH.toString(), rs.getString("attributelength"));
		meta.put(AttributeDataDefinitionMeta.PRECISION.toString(), String.valueOf(rs.getInt("attributeprecision")));
		meta.put(AttributeDataDefinitionMeta.SCALE.toString(), String.valueOf(rs.getInt("attributescale")));
		meta.put(AttributeDataDefinitionMeta.NOTNULL.toString(), String.valueOf(rs.getBoolean("attributenotnull")));
		meta.put(AttributeDataDefinitionMeta.UNIQUE.toString(), String.valueOf(rs.getBoolean("isunique")));
		meta.put(AttributeDataDefinitionMeta.DEFAULT.toString(), rs.getString("attributedefault"));
		meta.put(AttributeDataDefinitionMeta.LOCAL.toString(), String.valueOf(rs.getBoolean("attributeislocal")));
		return meta;
	}

	private AttributeType computeType(String dbTypeName, Map<String, String> meta) {
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

	private boolean isReference(Map<String, String> meta) {
		return meta.containsKey(AttributeDataDefinitionMeta.REFERENCEDOM.toString());
	}

	private boolean isForeignKey(Map<String, String> meta) {
		return meta.containsKey(AttributeDataDefinitionMeta.FKTARGETCLASS.toString());
	}

	private boolean isLookup(Map<String, String> meta) {
		return meta.containsKey(AttributeDataDefinitionMeta.LOOKUP.toString());
	}

	/*
	 * Domains
	 */

	@Override
	public void modifyDomain(IDomain domain) {
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
		try {
			stm = con.prepareCall(DomainQueries.MODIFY.toString());
			stm.setInt(1, domain.getId());
			stm.setString(2, createComment(domain));
			Log.SQL.debug(stm.toString());
			stm.execute();
			cache.refreshDomains();
		} catch (SQLException ex) {
			Log.PERSISTENCE.error("Errors modifying domain: " + domain.getId(), ex);
			cache.refreshDomains();
			throw ORMExceptionType.ORM_ERROR_DOMAIN_MODIFY.createException();
		} catch (RuntimeException re) {
			cache.refreshDomains();
			throw re;
		} finally {
			DBService.close(null, stm);
		}
	}

	@Override
	public int createDomain(IDomain domain) {
		int id;
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
		try {
			stm = con.prepareCall(DomainQueries.CREATE.toString());
			stm.registerOutParameter(1, Types.INTEGER);
			stm.setString(2, domain.getName());
			stm.setString(3, createComment(domain));
			Log.SQL.debug(stm.toString());
			stm.execute();
			id = stm.getInt(1);
			cache.refreshDomains();
		} catch (SQLException ex) {
			Log.PERSISTENCE.error("Errors creating new domain", ex);
			cache.refreshDomains();
			throw ORMExceptionType.ORM_ERROR_DOMAIN_CREATE.createException();
		} catch (RuntimeException re) {
			cache.refreshDomains();
			throw re;
		} finally {
			DBService.close(null, stm);
		}
		return id;
	}

	@Override
	public void deleteDomain(IDomain domain) {
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
		try {
			stm = con.prepareCall(DomainQueries.DELETE.toString());
			stm.setInt(1, domain.getId());
			Log.SQL.debug(stm.toString());
			stm.execute();
			cache.refreshDomains();
		} catch (SQLException ex) {
			Log.PERSISTENCE.error("Errors deleting domain", ex);
			throw ORMExceptionType.ORM_ERROR_DOMAIN_DELETE.createException();
		} finally {
			DBService.close(null, stm);
		}
	}

	@Override
	public Iterator<IDomain> getDomainList(DomainQuery query) {
		List<IDomain> list = new LinkedList<IDomain>();
		PreparedStatement stm = null;
		Connection con = DBService.getConnection();
		ResultSet rs = null;
		try {
			if (query.isInherited()) {
				Collection<String> ancestortree = TableImpl.tree().path(query.getTableName());
				String tablePath = StringUtils.join(ancestortree, ",");
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
					IDomain domain = cache.getDomain(rs.getInt("domainid"));
					list.add(domain);
				} catch (NotFoundException e) {
					Log.PERSISTENCE.debug("Domain table not found", e);
				}
			}
		} catch (SQLException ex) {
			Log.PERSISTENCE.error("Errors retrieving domains for class", ex);
		} catch (NotFoundException ex) {
			Log.PERSISTENCE.error("Errors retrieving class tree for retrieving hyerarchical domains", ex);
		} finally {
			DBService.close(rs, stm);
		}
		return list.iterator();
	}

	@Override
	public Iterable<IDomain> getDomainList() {
		return cache.getDomainList();
	}

	@Override
	public Map<Integer, IDomain> loadDomainMap() {
		Map<Integer, IDomain> map = new HashMap<Integer, IDomain>();
		Statement stm = null;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DBService.getConnection();
			stm = connection.createStatement();
			rs = stm.executeQuery(DomainQueries.FIND_ALL.toString());
			while (rs.next()) {
				Integer domainId = rs.getInt("domainid");
				String domainName = rs.getString("domainname");
				String domainComment = rs.getString("domaincomment");
				Log.PERSISTENCE.debug(String.format("Domain %s (%d) inserted into domain map", domainName, domainId));
				try {
					map.put(domainId, new DomainImpl(domainName, domainComment, domainId));
				} catch (CMDBException e) {
					Log.PERSISTENCE.error("Unable to add domain " + domainName);
				}
			}
		} catch (SQLException ex) {
			Log.PERSISTENCE.error("Errors retrieving all domains", ex);
		} finally {
			DBService.close(rs, stm);
		}
		return map;
	}

	/*
	 * Report (SHOULD BE REMOVED AFTER THE ARRAY AND BINARY ATTRIBUTE TYPES ARE
	 * IMPLEMENTED IN THE DAO LAYER)
	 */

	@Override
	public List<String> getReportTypes() {
		Statement stm = null;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DBService.getConnection();
			stm = connection.createStatement();
			rs = stm.executeQuery(ReportQueries.FIND_TYPES.toString());
			ArrayList<String> list = new ArrayList<String>();
			while (rs.next()) {
				list.add(rs.getString("Type"));
			}
			return list;
		} catch (Exception ex) {
			Log.REPORT.error("Errors retrieving report types", ex);
		} finally {
			DBService.close(rs, stm);
		}

		return null;
	}

	@Override
	public boolean insertReport(ReportCard bean) throws SQLException, IOException {
		PreparedStatement stm = null;
		String query = "INSERT INTO \"Report\" (\"Code\",\"Description\",\"Status\",\"User\",\"Type\",\"Query\",\"SimpleReport\",\"RichReport\",\"Wizard\",\"ReportLength\"                                                           ,\"Images\", \"ImagesLength\"                                                          , \"IdClass\" , \"Groups\"																		,\"ImagesName\")"
				+ " VALUES (?       ,?              ,?         ,?       ,?       ,?        ,?               ,?             ,?         ,cast(string_to_array('"
				+ arrayToCsv(bean.getReportLength())
				+ "',',') as int[]),?         ,cast(string_to_array('"
				+ arrayToCsv(bean.getImagesLength())
				+ "',',') as int[]), '\""
				+ ReportCard.REPORT_CLASS_NAME
				+ "\"', cast(string_to_array('"
				+ arrayToCsv(bean.getSelectedGroups())
				+ "',',') as int[]), cast(string_to_array('"
				+ arrayToCsv(bean.getImagesName())
				+ "',',') as varchar[])); ";
		Connection connection = null;
		int sr = 0;
		int rr = 0;
		int wr = 0;
		int im = 0;
		try {
			byte[] bin = null;
			connection = DBService.getConnection();
			stm = connection.prepareCall(query);
			stm.setString(1, bean.getCode());
			stm.setString(2, bean.getDescription());
			stm.setString(3, ElementStatus.ACTIVE.value());
			stm.setString(4, bean.getUser());
			stm.setString(5, bean.getType().toString().toLowerCase());
			stm.setString(6, bean.getQuery());

			bin = toByte(bean.getSimpleReport());
			sr = bin.length;
			stm.setBytes(7, bin);

			bin = toByte(bean.getRichReportBA());
			rr = bin.length;
			stm.setBytes(8, bin);

			bin = toByte(bean.getWizard());
			wr = bin.length;
			stm.setBytes(9, bin);

			bin = toByte(bean.getImagesBA());
			im = bin.length;
			stm.setBytes(10, bin);

			stm.execute();
			Log.REPORT
					.debug("sizes: SimpleReport=" + sr + " RichReport=" + rr + " WizardReport=" + wr + "Images:" + im);
			return true;
		} finally {
			DBService.close(null, stm);
		}
	}

	@Override
	public boolean updateReport(ReportCard bean) throws SQLException, IOException {
		PreparedStatement stm = null;
		Connection connection = null;

		String query = buildUpdateReportQuery(bean);

		try {
			int i = 1;
			connection = DBService.getConnection();
			stm = connection.prepareCall(query);

			stm.setString(i++, bean.getDescription());
			if (bean.getJd() != null) {
				i = setFileDependentAttrsInStm(bean, stm, i);
			}

			stm.setInt(i++, bean.getOriginalId());
			Log.SQL.debug(stm.toString());
			stm.executeUpdate();
			return true;
		} finally {
			DBService.close(null, stm);
		}
	}

	private int setFileDependentAttrsInStm(ReportCard bean, PreparedStatement stm, int i) throws SQLException,
			IOException {
		byte[] bin = null;
		stm.setString(i++, ElementStatus.ACTIVE.value());
		stm.setString(i++, bean.getUser());
		stm.setString(i++, bean.getType().toString().toLowerCase());
		stm.setString(i++, bean.getQuery());

		bin = toByte(bean.getSimpleReport());
		stm.setBytes(i++, bin);

		bin = toByte(bean.getRichReportBA());
		stm.setBytes(i++, bin);

		bin = toByte(bean.getWizard());
		stm.setBytes(i++, bin);

		bin = toByte(bean.getImagesBA());
		stm.setBytes(i++, bin);
		return i;
	}

	private String buildUpdateReportQuery(ReportCard bean) {
		final String queryBaseAttrsTM = "UPDATE \"Report\" SET \"Description\" = ?,\"Groups\" = cast(string_to_array('%s',',') as int[]) ";
		final String queryBaseAttrs = String.format(queryBaseAttrsTM, arrayToCsv(bean.getSelectedGroups()));

		final String queryFileDependentAttrsTM = ", \"Status\" = ?, \"User\" = ?, \"Type\" = ?, \"Query\" = ?,"
				+ "\"SimpleReport\" = ?, \"RichReport\" = ?, \"Wizard\" = ?, \"ReportLength\" = cast(string_to_array('%s',',') as int[]),"
				+ "\"Images\" = ?, \"ImagesLength\" = cast(string_to_array('%s',',') as int[]), \"IdClass\" = '\"%s\"', "
				+ "\"ImagesName\" = cast(string_to_array('%s',',') as varchar[]) ";

		final String queryEnd = "WHERE \"Id\" = ? ;";

		final String queryFileDependentAttrs = String.format(queryFileDependentAttrsTM,
				arrayToCsv(bean.getReportLength()), arrayToCsv(bean.getImagesLength()), ReportCard.REPORT_CLASS_NAME,
				arrayToCsv(bean.getImagesName()));

		String query = queryBaseAttrs;
		if (bean.getJd() != null) {
			query += queryFileDependentAttrs;
		}
		query += queryEnd;
		return query;
	}

	/*
	 * Lookup Types
	 */

	@Override
	public void createLookupType(LookupType lookupType) {
		String type = lookupType.getType();
		String parentType = lookupType.getParentTypeName();

		// can't create a lookup type with empty name
		if ((null == type) || "".equals(type)) {
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
		if ("".equals(parentType)) {
			parentType = null;
		}
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
		try {
			stm = con.prepareCall(LookupQueries.CREATE_LOOKUPTYPE.toString());
			stm.setString(1, type);
			stm.setString(2, parentType);
			Log.SQL.debug(stm.toString());
			stm.execute();
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors creating new lookup type", se);
			// TODO handle existing lookups
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			cache.refreshLookups();
			DBService.close(null, stm);
		}
	}

	@Override
	public void modifyLookupType(LookupType lookupType) {
		String type = lookupType.getType();
		String savedType = lookupType.getSavedType();
		// can't create a lookup type with empty name
		if ((null == type) || "".equals(type)) {
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
		try {
			stm = con.prepareCall(LookupQueries.MODIFY_LOOKUPTYPE.toString());
			stm.setString(1, type);
			stm.setString(2, savedType);
			stm.setString(3, type);
			stm.setString(4, savedType);
			Log.SQL.debug(stm.toString());
			stm.execute();
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors updating lookup type", se);
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			cache.refreshLookups();
			DBService.close(null, stm);
		}
	}

	@Override
	public void deleteLookupType(LookupType lookupType) {
		CallableStatement stm = null;
		Connection con = DBService.getConnection();
		try {
			stm = con.prepareCall(LookupQueries.DELETE_LOOKUPTYPE.toString());
			stm.setString(1, lookupType.getSavedType());
			Log.SQL.debug(stm.toString());
			stm.execute();
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors deleting lookup type", se);
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			cache.refreshLookups();
			DBService.close(null, stm);
		}
	}

	@Override
	public CTree<LookupType> loadLookupTypeTree() {
		Statement stm = null;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DBService.getConnection();
			stm = connection.createStatement();
			String query = LookupQueries.LOAD_TREE_TYPES.toString();
			Log.SQL.debug(query);
			rs = stm.executeQuery(query);
			return buildTree(rs);
		} catch (Exception ex) {
			Log.OTHER.error("Errors retrieving lookup tree", ex);
		} finally {
			DBService.close(rs, stm);
		}
		return new CTree<LookupType>();
	}

	private CTree<LookupType> buildTree(ResultSet rs) throws SQLException {
		// prepare hash
		HashMap<String, CNode<LookupType>> tempHash = new HashMap<String, CNode<LookupType>>();
		LookupType root = LookupType.createFromDB("root", null);
		while (rs.next()) {
			String type = rs.getString("Type");
			String parentType = rs.getString("ParentType");
			CNode<LookupType> node = new CNode<LookupType>();
			LookupType lookupType = LookupType.createFromDB(type, parentType);
			node.setData(lookupType);
			tempHash.put(lookupType.getType(), node);
		}

		// prepare tree
		CNode<LookupType> rootNode = new CNode<LookupType>();
		rootNode.setData(root);
		CTree<LookupType> tree = new CTree<LookupType>();
		tree.setRootElement(rootNode);

		for (CNode<LookupType> childNode : tempHash.values()) {
			CNode<LookupType> parentNode = tempHash.get(childNode.getData().getParentTypeName());
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
	public void modifyLookup(Lookup lookup) {
		Statement stm = null;
		Connection con = DBService.getConnection();
		try {
			LookupQueryBuilder qb = new LookupQueryBuilder();
			stm = con.createStatement();
			String query = qb.buildUpdateQuery(lookup);
			if (!query.equals("")) {
				Log.SQL.debug(query);
				stm.executeUpdate(query);
			}
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors modifying lookup", se);
			throw ORMExceptionType.ORM_ERROR_LOOKUP_MODIFY.createException();
		} finally {
			DBService.close(null, stm);
		}
	}

	@Override
	public int createLookup(Lookup lookup) {
		PreparedStatement stm = null;
		Connection con = DBService.getConnection();
		ResultSet rs = null;
		try {
			LookupQueryBuilder qb = new LookupQueryBuilder();
			String query = qb.buildInsertQuery(lookup);
			stm = con.prepareStatement(query);
			Log.SQL.debug(query);
			stm.executeQuery();
			rs = stm.getResultSet();
			if (!rs.next()) {
				Log.PERSISTENCE.error("Error retrieving generated primary key");
				throw ORMExceptionType.ORM_ERROR_GETTING_PK.createException();
			}
			return rs.getInt(1);
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors creating lookup", se);
			throw ORMExceptionType.ORM_ERROR_LOOKUP_CREATE.createException();
		} finally {
			cache.refreshLookups();
			DBService.close(null, stm);
		}
	}

	@Override
	public List<Lookup> findLookups() {
		List<Lookup> list = new LinkedList<Lookup>();
		Statement stm = null;
		Connection con = DBService.getConnection();
		ResultSet rs = null;
		try {
			ITable lookupTable = UserContext.systemContext().tables().get("LookUp");
			Collection<IAttribute> attributes = lookupTable.getAttributes().values();
			LookupQueryBuilder qb = new LookupQueryBuilder();
			String query = qb.buildSelectQuery();
			Map<String, QueryAttributeDescriptor> queryMapping = qb.getQueryComponents().getQueryMapping();
			stm = con.createStatement();
			Log.SQL.debug(query);
			rs = stm.executeQuery(query);
			while (rs.next()) {
				Lookup lookup = new Lookup();
				for (IAttribute attribute : attributes) {
					lookup.setValue(attribute.getName(), rs, queryMapping.get(attribute.getName()));
				}
				lookup.resetAttributes();
				list.add(lookup);
			}
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors finding cards", se);
		} catch (NotFoundException e) {
			return null;
		} finally {
			DBService.close(rs, stm);
		}
		return list;
	}

	/*
	 * Relation
	 */

	@Override
	public int createRelation(IRelation relation) {
		int id;
		PreparedStatement stm = null;
		Connection con = DBService.getConnection();
		ResultSet rs = null;
		try {
			RelationQueryBuilder qb = new RelationQueryBuilder();
			String query = qb.buildInsertQuery(relation);
			stm = con.prepareStatement(query);
			stm.executeQuery();
			rs = stm.getResultSet();
			if (rs.next()) {
				id = rs.getInt(1);
			} else {
				Log.PERSISTENCE.error("Error retrieving generated primary key");
				throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
			}
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors creating relation", se);
			throw ORMExceptionType.ORM_ERROR_RELATION_CREATE.createException();
		} finally {
			DBService.close(null, stm);
		}
		return id;
	}

	@Override
	public void modifyRelation(IRelation relation) {
		Statement stm = null;
		Connection con = DBService.getConnection();
		try {
			RelationQueryBuilder qb = new RelationQueryBuilder();
			String query = qb.buildUpdateQuery(relation);
			stm = con.createStatement();
			stm.executeUpdate(query);
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors modifying relation", se);
			throw ORMExceptionType.ORM_ERROR_RELATION_MODIFY.createException();
		} finally {
			DBService.close(null, stm);
		}
	}

	private static List<IRelation> perfomRelationQuery(IDomain domain, String query, QueryComponents queryComponents) {
		List<IRelation> list = new LinkedList<IRelation>();
		Statement stm = null;
		Connection con = DBService.getConnection();
		ResultSet rs = null;
		try {
			stm = con.createStatement();
			Log.SQL.debug(query);
			rs = stm.executeQuery(query);
			Map<String, QueryAttributeDescriptor> attrMapping1 = queryComponents.getQueryMapping("Table1");
			Map<String, QueryAttributeDescriptor> attrMapping2 = queryComponents.getQueryMapping("Table2");
			Map<String, QueryAttributeDescriptor> attrMappingM = queryComponents.getQueryMapping("Map");
			while (rs.next()) {
				try {
					CardImpl card1 = new CardImpl(
							rs.getInt(attrMappingM.get(IRelation.RelationAttributes.IdClass1.toString()).getValueAlias()));
					card1.setValue(ICard.CardAttributes.Id.toString(), rs,
							attrMappingM.get(IRelation.RelationAttributes.IdObj1.toString()));
					CardImpl card2 = new CardImpl(rs.getInt(attrMappingM.get(IRelation.RelationAttributes.IdClass2.toString())
							.getValueAlias()));
					card2.setValue(ICard.CardAttributes.Id.toString(), rs,
							attrMappingM.get(IRelation.RelationAttributes.IdObj2.toString()));
					for (String attrName : attrMapping1.keySet()) {
						card1.setValue(attrName, rs, attrMapping1.get(attrName));
					}
					for (String attrName : attrMapping2.keySet()) {
						card2.setValue(attrName, rs, attrMapping2.get(attrName));
					}
					IRelation relation = new RelationImpl(domain, card1, card2);
					for (String attrName : attrMappingM.keySet()) {
						relation.setValue(attrName, rs, attrMappingM.get(attrName));
					}
					relation.resetAttributes();
					list.add(relation);
				} catch (NotFoundException e) {
					Log.PERSISTENCE.debug("card in relation not found", e);
				}
			}
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors finding relations", se);
		} finally {
			DBService.close(rs, stm);
		}
		return list;
	}

	@Override
	public IRelation getRelation(IDomain domain, ICard card1, ICard card2) {
		RelationQueryBuilder qb = new RelationQueryBuilder();
		String query = qb.buildSelectQuery(domain, card1.getId(), card2.getId());
		Iterator<IRelation> relationIterator = perfomRelationQuery(domain, query, qb.getQueryComponents()).iterator();
		if (!relationIterator.hasNext()) {
			throw NotFoundExceptionType.NOTFOUND.createException();
		}
		return relationIterator.next();
	}

	@Override
	public IRelation getRelation(IDomain domain, int id) {
		RelationQueryBuilder qb = new RelationQueryBuilder();
		String query = qb.buildSelectQuery(domain, id);
		Iterator<IRelation> relationIterator = perfomRelationQuery(domain, query, qb.getQueryComponents()).iterator();
		if (!relationIterator.hasNext()) {
			throw NotFoundExceptionType.NOTFOUND.createException();
		}
		return relationIterator.next();
	}

	@Override
	public Iterable<IRelation> getRelationList(DirectedDomain directedDomain, int sourceId) {
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
	public int createCard(ICard card) {
		int id;
		PreparedStatement stm = null;
		Connection con = DBService.getConnection();
		ResultSet rs = null;
		try {
			CardQueryBuilder qb = new CardQueryBuilder();
			String query = qb.buildInsertQuery(card);
			stm = con.prepareStatement(query);
			stm.executeQuery();
			rs = stm.getResultSet();
			if (rs.next()) {
				id = rs.getInt(1);
			} else {
				Log.PERSISTENCE.error("Error retrieving generated primary key: is there a trigger ignoring the insert?");
				id = 0;
			}
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors creating card", se);
			SqlState.throwCustomExceptionFrom(se);
			return -1; // Never going to happen
		} finally {
			DBService.close(null, stm);
		}
		return id;
	}

	@Override
	public void modifyCard(ICard card) {
		Statement stm = null;
		Connection con = DBService.getConnection();
		try {
			CardQueryBuilder qb = new CardQueryBuilder();
			String query = qb.buildUpdateQuery(card);
			stm = con.createStatement();
			stm.executeUpdate(query);
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors modifying card", se);
			SqlState.throwCustomExceptionFrom(se);
		} finally {
			DBService.close(null, stm);
		}
	}

	@Override
	public List<ICard> getCardList(CardQueryImpl cardQuery) {
		List<ICard> list = new LinkedList<ICard>();
		Statement stm = null;
		Connection con = DBService.getConnection();
		ResultSet rs = null;
		try {
			CardQueryBuilder qb = new CardQueryBuilder();
			String query = cardQueryToSQL(cardQuery, qb);
			Map<String, QueryAttributeDescriptor> queryMapping = qb.getQueryComponents().getQueryMapping();
			stm = con.createStatement();
			rs = stm.executeQuery(query);

			int totalRows = 0;
			boolean countQuery = cardQuery.needsCount();
			boolean historyQuery = cardQuery.isHistory();
			ITable table = cardQuery.getTable();
			Set<String> attributes = cardQuery.getAttributes();

			while (rs.next()) {
				if (totalRows == 0 && countQuery)
					totalRows = rs.getInt("Count");
				ICard card;
				// to prevent inexistent tables to be requested (superclass
				// check for forged Tables like Menu)
				if (!table.isSuperClass() || historyQuery) {
					card = new CardImpl(table);
				} else {
					int classId = rs.getInt(queryMapping.get(ICard.CardAttributes.ClassId.toString()).getValueAlias());
					card = new CardImpl(classId);
				}
				for (String attributeName : attributes) {
					try {
						card.setValue(attributeName, rs, queryMapping.get(attributeName));
					} catch (NotFoundException e) {
						Log.SQL.error(String.format("Inexistent attribute \"%s\" for table \"%s\"", attributeName,
								table.getName()));
					}
				}
				card.resetAttributes();
				list.add(card);
			}
			cardQuery.setTotalRows(totalRows);
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors finding cards", se);
			throw ORMExceptionType.ORM_ERROR_CARD_SELECT.createException();
		} finally {
			DBService.close(rs, stm);
		}
		return list;
	}

	@Override
	public String cardQueryToSQL(CardQuery cardQuery, CardQueryBuilder qb) {
		Set<String> attributes = cardQuery.getAttributes();
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
	public int getCardPosition(CardQuery query, int cardId) {
		int position;
		// Automatically add all table attributes to the query if not specified
		// otherwise
		if (query.getAttributes().isEmpty()) {
			query.attributes(query.getTable().getAttributes().keySet().toArray(new String[0]));
		}
		Statement stm = null;
		Connection con = DBService.getConnection();
		ResultSet rs = null;
		try {
			CardQueryBuilder qb = new CardQueryBuilder();
			stm = con.createStatement();
			stm.executeQuery(CardQueryBuilder.CARD_ZERO_INDEX);
			rs = stm.executeQuery(qb.buildPositionQuery(query, cardId));
			if (rs.next()) {
				position = rs.getInt(1);
			} else {
				position = -1;
			}
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors getting card position", se);
			throw NotFoundExceptionType.CARD_NOTFOUND.createException(query.getTable().toString());
		} finally {
			DBService.close(rs, stm);
		}
		return position;
	}

	@Override
	public void updateCardsFromTemplate(CardQuery cardQuery, ICard cardTemplate) {
		Statement stm = null;
		Connection con = DBService.getConnection();
		ResultSet rs = null;
		try {
			CardQueryBuilder qb = new CardQueryBuilder();
			String query = qb.buildUpdateQuery(cardQuery, cardTemplate);
			stm = con.createStatement();
			stm.executeUpdate(query);
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors updating cards from template", se);
			throw ORMExceptionType.ORM_ERROR_CARD_UPDATE.createException();
		} finally {
			DBService.close(rs, stm);
		}
	}

	@Override
	public void deleteElement(IAbstractElement element) {
		Statement stm = null;
		Connection con = DBService.getConnection();
		ResultSet rs = null;
		try {
			String query = String.format("DELETE FROM \"%s\" WHERE \"Id\"=%d", element.getSchema().getDBName(),
					element.getId());
			Log.SQL.debug(query);
			stm = con.createStatement();
			stm.executeUpdate(query);
		} catch (SQLException se) {
			Log.PERSISTENCE.error("Errors deleting card", se);
			throw ORMExceptionType.ORM_ERROR_CARD_UPDATE.createException();
		} finally {
			DBService.close(rs, stm);
		}
	}

	/*
	 * Wrapper for Schema Cache
	 */

	public ITable getTable(String tableName) {
		return cache.getTable(tableName);
	}

	public ITable getTable(Integer classId) {
		return cache.getTable(classId);
	}

	public IDomain getDomain(String domainName) {
		return cache.getDomain(domainName);
	}

	public IDomain getDomain(Integer domainId) {
		return cache.getDomain(domainId);
	}

	public Lookup getLookup(Integer lookupId) {
		return cache.getLookup(lookupId);
	}

	public Lookup getLookup(String type, String description) {
		return cache.getLookup(type, description);
	}

	public Lookup getFirstLookupByCode(String type, String code) {
		return cache.getFirstLookupByCode(type, code);
	}

	public List<Lookup> getLookupList(String type, String description) {
		return cache.getLookupList(type, description);
	}

	public Iterable<LookupType> getLookupTypeList() {
		return cache.getLookupTypeList();
	}

	public CTree<LookupType> getLookupTypeTree() {
		return cache.getLookupTypeTree();
	}

	public LookupType getLookupType(final String type) {
		return cache.getLookupType(type);
	}

	public LookupType getLookupTypeOrDie(final String type) {
		final LookupType lt = getLookupType(type);
		if (lt == null) {
			throw NotFoundExceptionType.LOOKUP_NOTFOUND.createException(type);
		}
		return lt;
	}

	public Iterable<ITable> getTableList() {
		return cache.getTableList();
	}

	public TableTree getTableTree() {
		return cache.getTableTree();
	}

	/*
	 * FIXME
	 */

	public void clearCache() {
		cache.refreshTables();
		cache.refreshDomains();
		cache.refreshLookups();
	}
}
