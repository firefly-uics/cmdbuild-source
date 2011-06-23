package org.cmdbuild.dao.query.clause;

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entryType == null) ? 0 : entryType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryAliasAttribute other = (QueryAliasAttribute) obj;
		if (entryType == null) {
			if (other.entryType != null)
				return false;
		} else if (!entryType.equals(other.entryType))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
