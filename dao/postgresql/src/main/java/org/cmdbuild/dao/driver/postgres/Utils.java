package org.cmdbuild.dao.driver.postgres;

import static java.lang.String.format;
import static org.cmdbuild.dao.driver.postgres.Const.DOMAIN_PREFIX;
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


public class Utils {

	private Utils() {
		// prevents instantiation
	}

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
			quotedTypeName = format("%s(%s)", quoteIdent(functionCall.getFunction().getName()),
					functionParams(functionCall));
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

		private String genQuestionMarks(final int length) {
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
		return format("%s.%s", Utils.quoteAlias(tableAlias), Utils.quoteIdent(name));
	}

	public static Alias aliasForSystemAttribute(final Alias entityTypeAlias, final SystemAttributes sa) {
		return Alias.as(nameForSystemAttribute(entityTypeAlias, sa));
	}

	public static String nameForSystemAttribute(final Alias entityTypeAlias, final SystemAttributes sa) {
		return format("_%s_%s", entityTypeAlias.getName(), sa.name());
	}

	public static Alias aliasForUserAttribute(final Alias entityTypeAlias, final String name) {
		return Alias.as(nameForUserAttribute(entityTypeAlias, name));
	}

	public static String nameForUserAttribute(final Alias entityTypeAlias, final String name) {
		return format("%s#%s", entityTypeAlias.getName(), name);
	}

}
