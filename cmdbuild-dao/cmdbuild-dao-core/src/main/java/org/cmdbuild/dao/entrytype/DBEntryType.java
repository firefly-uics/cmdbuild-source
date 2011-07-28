package org.cmdbuild.dao.entrytype;

import static org.cmdbuild.dao.entrytype.Deactivable.IsActivePredicate.filterActive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.Metadata;

public abstract class DBEntryType implements CMEntryType {

	public static class EntryTypeMetadata extends Metadata {
		protected static final String BASE_NS = "system.entrytype.";

		public static final String DESCRIPTION = BASE_NS + "description";
		public static final String ACTIVE = BASE_NS + "active";
		public static final String MODE = BASE_NS + "mode";

		final String getDescription() {
			return get(DESCRIPTION);
		}

		final void setDescription(final String description) {
			put(DESCRIPTION, description);
		}

		final boolean isActive() {
			return Boolean.parseBoolean(get(ACTIVE));
		}

		final boolean isSystem() {
			return "reserved".equals(get(MODE)); // FIXME Use an enum and limit the valid values
		}
	}

	private final Object id;
	private final String name;
	private final EntryTypeMetadata meta;

	private final Map<String, DBAttribute> attributesByName;
	private final List<DBAttribute> attributes;

	protected DBEntryType(final String name, final Object id, final EntryTypeMetadata meta, final List<DBAttribute> attributes) {
		Validate.notEmpty(name);
		this.id = id;
		this.name = name;
		this.meta = meta;
		this.attributes = attributes;
		this.attributesByName = initAttributesByName(attributes);
	}

	private Map<String, DBAttribute> initAttributesByName(final List<DBAttribute> ac) {
		final Map<String, DBAttribute> am = new HashMap<String, DBAttribute>();
		for (DBAttribute a : ac) {
			a.owner = this;
			am.put(a.getName(), a);
		}
		return am;
	}

	protected EntryTypeMetadata getMeta() {
		return meta;
	}

	/*
	 * CMEntryType overrides
	 */

	@Override
	public Object getId() {
		return id;
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
	public boolean isSystem() {
		return meta.isSystem();
	}

	@Override
	public Iterable<DBAttribute> getAttributes() {
		return filterActive(getAllAttributes());
	}

	@Override
	public Iterable<DBAttribute> getAllAttributes() {
		return attributes;
	}

	@Override
	public DBAttribute getAttribute(final String name) {
		return attributesByName.get(name);
	}

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CMEntryType == false)
			return false;
		if (this == obj)
			return true;
		CMEntryType other = (CMEntryType) obj;
		return this.id.equals(other.getId());
	}

	@Override
	public String toString() {
		return name;
	}
}
