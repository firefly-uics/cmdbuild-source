package org.cmdbuild.dao.query.clause.alias;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMEntryType;

/*
 * It's basically a String but it is needed as a DTO
 */
public class Alias {

	private final String name;

	private Alias(final String name) {
		Validate.notEmpty(name);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/*
	 * Syntactic sugar
	 */
	public static Alias as(final String name) {
		return new Alias(name);
	}

	public static Alias canonicalAlias(final CMEntryType type) {
		return new Alias(type.getName());
	}

	public static Alias as(final Alias alias) {
		return alias;
	}

	/*
	 * Object overrides
	 */

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return name.equals(((Alias)obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}
