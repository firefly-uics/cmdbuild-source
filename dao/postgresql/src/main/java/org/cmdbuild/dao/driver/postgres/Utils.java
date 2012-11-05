package org.cmdbuild.dao.driver.postgres;

import static com.google.common.base.CharMatcher.DIGIT;
import static com.google.common.base.CharMatcher.inRange;
import static org.cmdbuild.dao.driver.postgres.Const.DOMAIN_PREFIX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.DBAttribute.AttributeMetadata;
import org.cmdbuild.dao.entrytype.DBClass.ClassMetadata;
import org.cmdbuild.dao.entrytype.DBDomain.DomainMetadata;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.function.CMFunction;
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
		if (inRange('a', 'z').or(DIGIT).matchesAllOf(name)) {
			return name;
		} else {
			return String.format("\"%s\"", name.replace("\"", "\"\""));
		}
	}

	public static String quoteIdent(final SystemAttributes sa) {
		return quoteIdent(sa.getDBName());
	}

	static class TypeQuoter implements CMEntryTypeVisitor {

		private ParamAdder paramAdder;
		private String quotedTypeName;

		public String quote(final CMEntryType type,  final ParamAdder paramAdder) {
			this.paramAdder = paramAdder;
			type.accept(this);
			return quotedTypeName;
		}

		@Override
		public void visit(final CMClass type) {
			quotedTypeName = quoteIdent(type.getName());
		}

		@Override
		public void visit(final CMDomain type) {
			quotedTypeName = quoteIdent(DOMAIN_PREFIX + type.getName());
		}

		@Override
		public void visit(final CMFunctionCall functionCall) {
			quotedTypeName = String.format("%s(%s)",
					quoteIdent(functionCall.getFunction().getName()),
					functionParams(functionCall)
				);
		}

		private String functionParams(final CMFunctionCall functionCall) {
			final List<CMFunction.CMFunctionParameter> functionParameters = functionCall.getFunction()
					.getInputParameters();
			final List<Object> params = functionCall.getParams();
			for (int i = 0; i < functionParameters.size(); i++) {
				final CMFunction.CMFunctionParameter functionParameter = functionParameters.get(i);
				final Object param = params.get(i);
				final SqlType sqlType = SqlType.getSqlType(functionParameter.getType());
				paramAdder.add(sqlType.javaToSqlValue(param));
			}
			return genQuestionMarks(functionCall.getParams().size());
		}

		private String genQuestionMarks(int length) {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < length; ++i) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append("?");
			}
			return sb.toString();
		}
	}
	
	// TODO should not use this trick for add parameters to the part creator
	public static interface ParamAdder {
		public void add(Object value);
	}

	// TODO: params adder is needed for functions... should be done in another way
	public static String quoteType(final CMEntryType type, final ParamAdder paramAdder) {
		return new TypeQuoter().quote(type, paramAdder);
	}

	/**
	 * @deprecated But first the other one needs to be fixed!
	 */
	@Deprecated
	public static String quoteType(final CMEntryType type) {
		return new TypeQuoter().quote(type, new ParamAdder() {
			@Override
			public void add(final Object value) {
				// nothing to do
			}
		});
	}

	static class EntryTypeHistoryQuoter implements CMEntryTypeVisitor {

		String quotedTypeName;

		public String quote(final CMEntryType type) {
			type.accept(this);
			return quotedTypeName;
		}

		@Override
		public void visit(final CMClass type) {
			quotedTypeName = quoteIdent(type.getName() + Const.HISTORY_SUFFIX);
		}

		@Override
		public void visit(final CMDomain type) {
			quotedTypeName = quoteIdent(DOMAIN_PREFIX + type.getName() + Const.HISTORY_SUFFIX);
		}

		@Override
		public void visit(final CMFunctionCall type) {
			throw new UnsupportedOperationException("Cannot specify history for functions");
		}
	}

	public static String quoteTypeHistory(final CMEntryType type) {
		return new EntryTypeHistoryQuoter().quote(type);
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
