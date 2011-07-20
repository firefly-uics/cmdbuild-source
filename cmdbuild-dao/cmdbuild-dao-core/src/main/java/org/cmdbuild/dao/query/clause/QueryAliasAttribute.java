package org.cmdbuild.dao.query.clause;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;

public class QueryAliasAttribute implements QueryAttribute {

	private final String name;
	private final Alias entryType;

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

	public static QueryAliasAttribute attribute(final Alias entryType, final String name) {
		return new QueryAliasAttribute(entryType, name);
	}

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.entryType).append(this.name).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof QueryAliasAttribute == false)
			return false;
		if (this == obj)
			return true;
		QueryAliasAttribute other = (QueryAliasAttribute) obj;
		return new EqualsBuilder().append(this.entryType, other.entryType).append(this.name, other.name).isEquals();
	}
}
