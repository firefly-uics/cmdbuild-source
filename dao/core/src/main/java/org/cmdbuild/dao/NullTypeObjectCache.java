package org.cmdbuild.dao;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBClass;

public class NullTypeObjectCache implements TypeObjectCache {

	@Override
	public void add(final CMTypeObject typeObject) {
		// do nothing
	}

	@Override
	public void remove(final CMTypeObject typeObject) {
		// do nothing
	}

	@Override
	public <T extends CMTypeObject> T fetch(final Class<? extends CMTypeObject> typeObjectClass, final Long id) {
		return null;
	}

	@Override
	public <T extends CMTypeObject> T fetch(final Class<? extends CMTypeObject> typeObjectClass,
			final CMIdentifier identifier) {
		return null;
	}

	@Override
	public void clearCache() {
		// do nothing
	}

	@Override
	public void clearClasses() {
		// do nothing
	}

	@Override
	public void clearDomains() {
		// do nothing
	}

	@Override
	public void clearFunctions() {
		// do nothing
	}

	@Override
	public boolean hasNoClass() {
		return false;
	}

	@Override
	public List<DBClass> fetchCachedClasses() {
		return null;
	}

}
