package org.cmdbuild.dao.reference;

public abstract class AbstractReference implements CMReference {

	private Object id;

	protected AbstractReference(final Object id) {
		this.id = id;
	}

	@Override
	public final Object getId() {
		return id;
	}

}
