package org.cmdbuild.dao.entrytype;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public class DBAttribute implements CMAttribute {

	public static class AttributeMetadata extends EntryTypeMetadata {
	}

	CMEntryType owner; // Set by the entry type when attached
	private final CMAttributeType<?> type;

	// TODO Make name and meta inherited by both DBAttribute and DBEntryType
	private final String name;
	private final AttributeMetadata meta;

	public DBAttribute(final String name, final CMAttributeType<?> type) {
		this(name, type, new AttributeMetadata());
	}

	public DBAttribute(final String name, final CMAttributeType<?> type, final AttributeMetadata meta) {
		Validate.notEmpty(name);
		this.owner = UndefinedClass.UNDEFINED_CLASS;
		this.name = name;
		this.type = type;
		this.meta = meta;
	}

	@Override
	public CMEntryType getOwner() {
		return owner;
	}

	@Override
	public CMAttributeType<?> getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return meta.getDescription();
	}

	@Override
	public boolean isSystem() {
		return meta.isSystem();
	}

	/*
	 * Object overrides
	 */

	@Override
	public String toString() {
		return String.format("%s.%s", owner, name);
	}
}
