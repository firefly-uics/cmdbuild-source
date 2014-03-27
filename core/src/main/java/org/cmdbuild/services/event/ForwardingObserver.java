package org.cmdbuild.services.event;

import org.cmdbuild.dao.entry.CMCard;

public abstract class ForwardingObserver implements Observer {

	private final Observer delegate;

	protected ForwardingObserver(final Observer delegate) {
		this.delegate = delegate;
	}

	@Override
	public void afterCreate(final CMCard actual) {
		delegate.afterCreate(actual);
	}

	@Override
	public void beforeUpdate(final CMCard actual, final CMCard next) {
		delegate.beforeUpdate(actual, next);
	}

	@Override
	public void afterUpdate(final CMCard previous, final CMCard actual) {
		delegate.afterUpdate(previous, actual);
	}

	@Override
	public void beforeDelete(final CMCard actual) {
		delegate.beforeDelete(actual);
	}

}