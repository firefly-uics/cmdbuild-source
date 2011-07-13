package org.cmdbuild.dao.entrytype;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;

public class DBAttribute implements CMAttribute {

	public static class AttributeMetadata extends EntryTypeMetadata {
	}

	CMEntryType owner;

	// TODO Make name and meta inherited by both DBAttribute and DBEntryType
	private final String name;
	private final AttributeMetadata meta;

	public DBAttribute(final String name) {
		this(name, new AttributeMetadata());
	}

	public DBAttribute(final String name, final AttributeMetadata meta) {
		Validate.notEmpty(name);
		this.owner = null;
		this.name = name;
		this.meta = meta;
	}

	@Override
	public CMEntryType getOwner() {
		return owner;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isSystem() {
		return meta.isSystem();
	}
}
