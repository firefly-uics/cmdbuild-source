package org.cmdbuild.dao.reference;

public abstract class AbstractReference implements CMReference {

	private final Long id;

	protected AbstractReference(final Long id) {
		this.id = id;
	}

	@Override
	public final Long getId() {
		return id;
	}

}
