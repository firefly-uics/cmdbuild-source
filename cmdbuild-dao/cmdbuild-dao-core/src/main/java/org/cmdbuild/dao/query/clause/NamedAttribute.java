package org.cmdbuild.dao.query.clause;


public class NamedAttribute implements QueryAttribute {

	private final String entryTypeAliasName;
	private final String name;

	public NamedAttribute(final String fullname) {
		String[] split = fullname.split("\\.");
		switch (split.length) {
		case 1:
			entryTypeAliasName = null;
			name = split[0];
			break;
		case 2:
			entryTypeAliasName = split[0];
			name = split[1];
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public final String getName() {
		return name;
	}

	public final String getEntryTypeAliasName() {
		return entryTypeAliasName;
	}
}
