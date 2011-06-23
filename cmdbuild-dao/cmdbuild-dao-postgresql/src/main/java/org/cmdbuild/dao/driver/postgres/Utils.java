package org.cmdbuild.dao.driver.postgres;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;

public class Utils {

	public static final String ID_ATTRIBUTE = "Id";
	public static final String CLASS_ID_ATTRIBUTE = "IdClass";
	public static final String DOMAIN_ID_ATTRIBUTE = "IdDomain";

	public static final String CODE_ATTRIBUTE = "Code";
	public static final String DESCRIPTION_ATTRIBUTE = "Description";

	public static final String DOMAIN_ID1_ATTRIBUTE = "IdObj1";
	public static final String DOMAIN_ID2_ATTRIBUTE = "IdObj2";

	public static final String STATUS_ATTRIBUTE = "Status";
	public static final String STATUS_ACTIVE_VALUE = "A";

	static final String OPERATOR_EQ = "=";

	static final String DOMAIN_PREFIX = "Map_";
	static final String HISTORY_SUFFIX = "_history";

	enum DomainCommentMeta {
		CLASS1,
		CLASS2;
	}

	private Utils() {};

	static String quoteIdent(final String name) {
		return String.format("\"%s\"", name.replace("\"", "\"\""));
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

	static String quoteAttribute(final Alias tableAlias, final String name) {
		return String.format("%s.%s", quoteAlias(tableAlias), quoteIdent(name));
	}

	static String quoteAttribute(final Alias tableAlias, final String name, final String typeCast) {
		return String.format("%s::%s", quoteAttribute(tableAlias, name), typeCast);
	}

	static String quoteAlias(final Alias alias) {
		return quoteIdent(alias.getName());
	}
}
