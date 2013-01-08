package org.cmdbuild.dao;

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
	public <T extends CMTypeObject> T fetch(final Class<? extends CMTypeObject> typeObjectClass, final String name) {
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
}
