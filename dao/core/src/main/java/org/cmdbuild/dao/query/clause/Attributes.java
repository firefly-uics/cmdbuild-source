package org.cmdbuild.dao.query.clause;

import static org.cmdbuild.dao.query.clause.alias.Aliases.name;

import org.cmdbuild.dao.query.clause.alias.Alias;

public class Attributes {

	public static QueryAttribute named(final String value) {
		final Alias alias;
		final String name;
		final String[] split = value.split("\\.");
		switch (split.length) {
		case 1:
			alias = null;
			name = split[0];
			break;
		case 2:
			alias = name(split[0]);
			name = split[1];
			break;
		default:
			throw new IllegalArgumentException(value);
		}
		return new QueryAliasAttribute(alias, name);
	}

	/**
	 * Prevents instantiation.
	 */
	private Attributes() {
	}

}
