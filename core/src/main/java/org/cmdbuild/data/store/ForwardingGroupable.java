package org.cmdbuild.data.store;

public abstract class ForwardingGroupable implements Groupable {

	private final Groupable delegate;

	protected ForwardingGroupable(final Groupable delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getGroupAttributeName() {
		return delegate.getGroupAttributeName();
	}

	@Override
	public Object getGroupAttributeValue() {
		return delegate.getGroupAttributeValue();
	}

}
