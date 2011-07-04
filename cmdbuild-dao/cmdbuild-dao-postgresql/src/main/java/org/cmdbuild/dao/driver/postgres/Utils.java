package org.cmdbuild.dao.driver.postgres;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBClass.ClassMetadata;
import org.cmdbuild.dao.entrytype.DBDomain.DomainMetadata;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public abstract class Utils {

	/*
	 * FIXME We should use generic identifiers as _Code instead of Code directly
	 */

	public static final String CODE_ATTRIBUTE = SystemAttributes.Code.getDBName();
	public static final String DESCRIPTION_ATTRIBUTE = SystemAttributes.Description.getDBName();
	public static final String ID_ATTRIBUTE = SystemAttributes.Id.getDBName();

	/*
	 * Constants
	 */

	enum SystemAttributes {
		Id("Id"),
		ClassId("IdClass", "oid"),
		DomainId("IdDomain", "oid"),
		DomainId1("IdObj1"),
		DomainId2("IdObj2"),
		Code("Code"),
		Description("Description"),
		BeginDate("BeginDate"),
		Status("Status"),
		// Fake attributes
		DomainQuerySource("_Src"),
		DomainQueryTargetId("_DstId"),
		;

		final String dbName;
		final String castSuffix;

		SystemAttributes(final String dbName, final String typeCast) {
			this.dbName = dbName;
			this.castSuffix = (typeCast != null) ? "::" + typeCast : "";
		}

		SystemAttributes(final String dbName) {
			this(dbName, null);
		}

		public String getDBName() {
			return dbName;
		}

		public String getCastSuffix() {
			return castSuffix;
		}
	}

	public static final String STATUS_ACTIVE_VALUE = "A";

	static final String OPERATOR_EQ = "=";

	static final String DOMAIN_PREFIX = "Map_";
	static final String HISTORY_SUFFIX = "_history";

	/*
	 * Comment <-> meta
	 */

	private interface CommentValueConverter {
		String getMetaValueFromComment(String commentValue);
		String getCommentValueFromMeta(String metaValue);
	}

	static class CommentMapper {
		private final BiMap<String, String> translationTable = HashBiMap.create();
		private final Map<String, CommentValueConverter> valueConverterTable = new HashMap<String, CommentValueConverter>();

		public String getMetaNameFromComment(final String commentName) {
			return translationTable.get(commentName);
		}

		public String getCommentNameFromMeta(final String metaName) {
			return translationTable.inverse().get(metaName);
		}

		public String getMetaValueFromComment(final String commentName, final String commentValue) {
			if (valueConverterTable.containsKey(commentName)) {
				return valueConverterTable.get(commentName).getMetaValueFromComment(commentValue);
			} else {
				return commentValue;
			}
		}

		public String getCommentValueFromMeta(final String commentName, final String metaValue) {
			if (valueConverterTable.containsKey(commentName)) {
				return valueConverterTable.get(commentName).getCommentValueFromMeta(metaValue);
			} else {
				return metaValue;
			}
		}

		protected void define(final String commentName, final String metaName) {
			translationTable.put(commentName, metaName);
		}

		protected void define(final String commentName, final String metaName, final CommentValueConverter valueConverter) {
			translationTable.put(commentName, metaName);
			if (valueConverter != null) {
				valueConverterTable.put(commentName, valueConverter);
			}
		}
	}

	static class EntryTypeCommentMapper extends CommentMapper {
		{
			define("STATUS", EntryTypeMetadata.ACTIVE, new CommentValueConverter() {
				@Override public String getMetaValueFromComment(String commentValue) {
					// Set to active by default for backward compatibility
					return "noactive".equalsIgnoreCase(commentValue) ? Boolean.FALSE.toString() : Boolean.TRUE.toString();
				}
				@Override public String getCommentValueFromMeta(String metaValue) {
					return Boolean.parseBoolean(metaValue) ? "active" : "noactive";
				}
			});
		}
	}

	static final CommentMapper CLASS_COMMENT_MAPPER = new EntryTypeCommentMapper() {
		{
			define("DESCR", EntryTypeMetadata.DESCRIPTION);
			define("SUPERCLASS", ClassMetadata.SUPERCLASS);
		}
	};

	static final CommentMapper DOMAIN_COMMENT_MAPPER = new EntryTypeCommentMapper() {
		{
			define("LABEL", EntryTypeMetadata.DESCRIPTION); // Excellent name choice!
			define("CLASS1", DomainMetadata.CLASS_1);
			define("CLASS2", DomainMetadata.CLASS_2);
			// The descriptions should be the attribute descriptions to support n-ary domains
			define("DESCRDIR", DomainMetadata.DESCRIPTION_1);
			define("DESCRINV", DomainMetadata.DESCRIPTION_2);
		}
	};

	/*
	 * Utility functions
	 */

	static String quoteIdent(final String name) {
		return String.format("\"%s\"", name.replace("\"", "\"\""));
	}

	static String quoteIdent(final SystemAttributes sa) {
		return quoteIdent(sa.getDBName());
	}

	static String quoteType(final CMEntryType type) {
		return quoteIdent(getTypeName(type));
	}

	static String getTypeName(CMEntryType type) {
		if (type instanceof CMDomain) {
			return domainNameToTableName(type.getName());
		} else {
			return type.getName();
		}
	}

	static String domainNameToTableName(final String domainName) {
		return DOMAIN_PREFIX + domainName;
	}

	static String tableNameToDomainName(final String tableName) {
		if (!tableName.startsWith(DOMAIN_PREFIX)) {
			throw new IllegalArgumentException("Domains should start with " + DOMAIN_PREFIX);
		}
		return tableName.substring(DOMAIN_PREFIX.length());
	}

	static String quoteAttribute(final QueryAliasAttribute attribute) {
		return quoteAttribute(attribute.getEntryTypeAlias(), attribute.getName());
	}

	static String quoteAttribute(final Alias tableAlias, final SystemAttributes sa) {
		return quoteAttribute(tableAlias, sa.getDBName());
	}

	static String quoteAttribute(final Alias tableAlias, final String name) {
		return String.format("%s.%s", quoteAlias(tableAlias), quoteIdent(name));
	}

	static String quoteAlias(final Alias alias) {
		return quoteIdent(alias.getName());
	}
}
