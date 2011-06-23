package org.cmdbuild.dao.entrytype;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;

public class DBAttribute implements CMAttribute {

	public static class AttributeMetadata extends EntryTypeMetadata {
	}

	CMEntryType owner;
	private final String name;

	public DBAttribute(final String name) {
		this(name, new AttributeMetadata());
	}

	public DBAttribute(final String name, final AttributeMetadata meta) {
		Validate.notEmpty(name);
		this.owner = null;
		this.name = name;
	}

	@Override
	public CMEntryType getOwner() {
		return owner;
	}

	@Override
	public String getName() {
		return name;
	}
}
