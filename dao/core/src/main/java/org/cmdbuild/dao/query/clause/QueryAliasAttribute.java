package org.cmdbuild.dao.query.clause;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;

/**
 * Represents a single attribute in a query.
 * 
 * For example: "TableName"."AttributeName"
 */
public class QueryAliasAttribute implements QueryAttribute {

	private final String name;
	private final Alias entryType;

	/**
	 * Creates a new {@link QueryAliasAttribute}.
	 * 
	 * @param entryType
	 *            is the alias of the entry type (e.g. the table of a database).
	 * @param name
	 *            if the name of the attribute.
	 */
	protected QueryAliasAttribute(final Alias entryType, final String name) {
		this.entryType = entryType;
		this.name = name;
	}

	public Alias getEntryTypeAlias() {
		return entryType;
	}

	public String getName() {
		return name;
	}

	public static QueryAliasAttribute attribute(final CMEntryType type, final String name) {
		return attribute(Alias.canonicalAlias(type), name);
	}

	public static QueryAliasAttribute attribute(final String entryTypeName, final String name) {
		return attribute(Alias.as(entryTypeName), name);
	}

	public static QueryAliasAttribute attribute(final Alias entryTypeAlias, final String name) {
		return new QueryAliasAttribute(entryTypeAlias, name);
	}

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(entryType) //
				.append(name) //
				.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof QueryAliasAttribute == false) {
			return false;
		}
		final QueryAliasAttribute other = QueryAliasAttribute.class.cast(obj);
		return new EqualsBuilder() //
				.append(entryType, other.entryType) //
				.append(name, other.name) //
				.isEquals();
	}

}
