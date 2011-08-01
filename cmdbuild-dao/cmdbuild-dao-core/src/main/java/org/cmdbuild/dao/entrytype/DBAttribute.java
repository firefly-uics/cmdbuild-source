package org.cmdbuild.dao.entrytype;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public class DBAttribute implements CMAttribute {

	public static class AttributeMetadata extends EntryTypeMetadata {
		public static final String LOOKUP_TYPE = BASE_NS + "lookuptype";

		public final boolean isLookup() {
			return (getLookupType() != null);
		}

		public final String getLookupType() {
			final String lookupTypeName = get(LOOKUP_TYPE);
			if (lookupTypeName == null || lookupTypeName.trim().isEmpty()) {
				return null;
			} else {
				return lookupTypeName;
			}
		}
	}

	DBEntryType owner; // Set by the entry type when attached
	private final CMAttributeType<?> type;

	// TODO Make name and meta inherited by both DBAttribute and DBEntryType
	private final String name;
	private final AttributeMetadata meta;

	public DBAttribute(final String name, final CMAttributeType<?> type, final AttributeMetadata meta) {
		Validate.notEmpty(name);
		this.owner = null; // Use a null object?
		this.name = name;
		this.type = type;
		this.meta = meta;
	}

	@Override
	public DBEntryType getOwner() {
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
	public boolean isActive() {
		return meta.isActive();
	}

	/*
	 * Object overrides
	 */

	@Override
	public String toString() {
		return String.format("%s.%s", owner, name);
	}
}
