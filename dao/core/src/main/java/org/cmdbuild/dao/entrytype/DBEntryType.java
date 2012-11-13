package org.cmdbuild.dao.entrytype;

import static org.cmdbuild.dao.entrytype.Deactivable.IsActivePredicate.filterActive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.DBTypeObject;
import org.cmdbuild.dao.Metadata;

public abstract class DBEntryType extends DBTypeObject implements CMEntryType {

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
			final String value = get(ACTIVE);
			return Boolean.parseBoolean((value == null) ? Boolean.TRUE.toString() : value);
		}

		final boolean isSystem() {
			// FIXME Use an enum and limit the valid values
			return "reserved".equals(get(MODE));
		}
	}

	private final Map<String, DBAttribute> attributesByName;
	private final List<DBAttribute> attributes;

	protected DBEntryType(final String name, final Long id, final List<DBAttribute> attributes) {
		super(name, id);
		this.attributes = attributes;
		this.attributesByName = initAttributesByName(attributes);
	}

	public abstract void accept(DBEntryTypeVisitor visitor);

	private Map<String, DBAttribute> initAttributesByName(final List<DBAttribute> ac) {
		final Map<String, DBAttribute> am = new HashMap<String, DBAttribute>();
		for (final DBAttribute a : ac) {
			a.owner = this;
			am.put(a.getName(), a);
		}
		return am;
	}

	protected abstract EntryTypeMetadata meta();

	/*
	 * CMEntryType overrides
	 */

	@Override
	public String getDescription() {
		return meta().getDescription();
	}

	@Override
	public boolean isActive() {
		return meta().isActive();
	}

	@Override
	public boolean isSystem() {
		return meta().isSystem();
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

	@Override
	public String getKeyAttributeName() {
		// TODO Mark it in the metadata!
		return "Id";
	}

}
