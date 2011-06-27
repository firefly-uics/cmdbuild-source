package org.cmdbuild.dao.entrytype;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.Metadata;

public abstract class DBEntryType implements CMEntryType {

	public static class EntryTypeMetadata extends Metadata {
		protected static final String BASE_NS = "system.entrytype.";

		public static final String DESCRIPTION = BASE_NS + "description";
		public static final String ACTIVE = BASE_NS + "active";

		final String getDescription() {
			return get(DESCRIPTION);
		}

		final void setDescription(final String description) {
			put(DESCRIPTION, description);
		}

		final boolean isActive() {
			return Boolean.parseBoolean(get(ACTIVE));
		}
	}

	private final Object id;
	private final String name;
	private final EntryTypeMetadata meta;

	private final Map<String, DBAttribute> attributes;

	protected DBEntryType(final String name, final Object id, final EntryTypeMetadata meta, final Collection<DBAttribute> attributes) {
		Validate.notEmpty(name);
		this.id = id;
		this.name = name;
		this.meta = meta;
		this.attributes = initAttributes(attributes);
	}

	private Map<String, DBAttribute> initAttributes(final Collection<DBAttribute> ac) {
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
	public Iterable<DBAttribute> getAttributes() {
		return attributes.values();
	}

	@Override
	public DBAttribute getAttribute(final String name) {
		return attributes.get(name);
	}

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		DBEntryType other = (DBEntryType) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
