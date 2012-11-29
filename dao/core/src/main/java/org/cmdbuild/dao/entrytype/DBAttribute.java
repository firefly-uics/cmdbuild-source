package org.cmdbuild.dao.entrytype;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public class DBAttribute implements CMAttribute {

	public static class AttributeMetadata extends EntryTypeMetadata implements CMAttributeType.Meta {

		public static final String BASEDSP = BASE_NS + "basedsp";
		public static final String LOOKUP_TYPE = BASE_NS + "lookuptype";
		public static final String MANDATORY = BASE_NS + "mandatory";
		public static final String UNIQUE = BASE_NS + "unique";

		@Override
		public final boolean isLookup() {
			return (getLookupType() != null);
		}

		@Override
		public final String getLookupType() {
			final String lookupTypeName = get(LOOKUP_TYPE);
			if (lookupTypeName == null || lookupTypeName.trim().isEmpty()) {
				return null;
			} else {
				return lookupTypeName;
			}
		}

		public boolean isDisplayableInList() {
			return Boolean.parseBoolean(get(BASEDSP));
		}

		public boolean isMandatory() {
			return Boolean.parseBoolean(get(MANDATORY));
		}

		public boolean isUnique() {
			return Boolean.parseBoolean(get(UNIQUE));
		}

		public Mode getMode() {
			// TODO do it better... and remember that tests are our friends!
			final Mode fieldMode;
			final String mode = get(MODE);
			if ("hidden".equals(mode)) {
				fieldMode = Mode.HIDDEN;
			} else if ("read".equals(mode)) {
				fieldMode = Mode.READ;
			} else {
				fieldMode = Mode.WRITE;
			}
			return fieldMode;
		}
	}

	DBEntryType owner; // Set by the entry type when attached
	private final CMAttributeType<?> type;

	// TODO Make name and meta inherited by both DBAttribute and DBEntryType
	private final String name;
	private final AttributeMetadata meta;

	public DBAttribute(final String name, final CMAttributeType<?> type, final AttributeMetadata meta) {
		Validate.notEmpty(name);
		this.owner = null; // TODO Use a null object?
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

	@Override
	public boolean isDisplayableInList() {
		return meta.isDisplayableInList();
	}

	@Override
	public boolean isMandatory() {
		return meta.isMandatory();
	}

	@Override
	public boolean isUnique() {
		return meta.isUnique();
	}

	@Override
	public Mode getMode() {
		return meta.getMode();
	}

	/*
	 * Object overrides
	 */

	@Override
	public String toString() {
		return String.format("%s.%s", owner, name);
	}
}
