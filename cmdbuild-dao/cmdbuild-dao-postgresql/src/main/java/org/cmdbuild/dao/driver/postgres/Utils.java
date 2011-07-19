package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Const.DOMAIN_PREFIX;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBAttribute.AttributeMetadata;
import org.cmdbuild.dao.entrytype.DBClass.ClassMetadata;
import org.cmdbuild.dao.entrytype.DBDomain.DomainMetadata;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;


public abstract class Utils {

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
			define("MODE", EntryTypeMetadata.MODE, new CommentValueConverter() {
				@Override public String getMetaValueFromComment(String commentValue) {
					return commentValue.toLowerCase().trim();
				}
				@Override public String getCommentValueFromMeta(String metaValue) {
					return metaValue;
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

	static final CommentMapper ATTRIBUTE_COMMENT_MAPPER = new EntryTypeCommentMapper() {
		{
			define("DESCR", EntryTypeMetadata.DESCRIPTION);
			define("LOOKUP", AttributeMetadata.LOOKUP_TYPE);
		}
	};

	/*
	 * Utility functions
	 */

	public static String quoteIdent(final String name) {
		return String.format("\"%s\"", name.replace("\"", "\"\""));
	}

	public static String quoteIdent(final SystemAttributes sa) {
		return quoteIdent(sa.getDBName());
	}

	public static String quoteType(final CMEntryType type) {
		return quoteIdent(getTypeName(type));
	}

	public static String quoteTypeHistory(final CMEntryType type) {
		return quoteIdent(getTypeName(type) + Const.HISTORY_SUFFIX);
	}

	public static String getTypeName(CMEntryType type) {
		if (type instanceof CMDomain) {
			return domainNameToTableName(type.getName());
		} else {
			return type.getName();
		}
	}

	public static String domainNameToTableName(final String domainName) {
		return DOMAIN_PREFIX + domainName;
	}

	public static String tableNameToDomainName(final String tableName) {
		if (!tableName.startsWith(DOMAIN_PREFIX)) {
			throw new IllegalArgumentException("Domains should start with " + DOMAIN_PREFIX);
		}
		return tableName.substring(DOMAIN_PREFIX.length());
	}

	public static String quoteAlias(final Alias alias) {
		return quoteIdent(alias.getName());
	}

	public static String quoteAttribute(final QueryAliasAttribute attribute) {
		return quoteAttribute(attribute.getEntryTypeAlias(), attribute.getName());
	}

	public static String quoteAttribute(final Alias tableAlias, final SystemAttributes sa) {
		return quoteAttribute(tableAlias, sa.getDBName());
	}

	public static String quoteAttribute(final Alias tableAlias, final String name) {
		return String.format("%s.%s", Utils.quoteAlias(tableAlias), Utils.quoteIdent(name));
	}

	public static String getSystemAttributeAlias(final Alias entityTypeAlias, final SystemAttributes sa) {
		return String.format("_%s_%s", entityTypeAlias.getName(), sa.name());
	}
}
