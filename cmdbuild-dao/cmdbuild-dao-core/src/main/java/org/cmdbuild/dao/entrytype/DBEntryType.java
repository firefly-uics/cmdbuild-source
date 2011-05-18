package org.cmdbuild.dao.entrytype;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

public abstract class DBEntryType implements CMEntryType {

	private final Object id;
	private final String name;

	private final Map<String, DBAttribute> attributes;

	protected DBEntryType(final String name, final Object id, final Collection<DBAttribute> attributes) {
		Validate.notEmpty(name);
		this.id = id;
		this.name = name;
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

	@Override
	public Object getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Iterable<DBAttribute> getAttributes() {
		return attributes.values();
	}

	@Override
	public DBAttribute getAttribute(final String name) {
		return attributes.get(name);
	}
}
