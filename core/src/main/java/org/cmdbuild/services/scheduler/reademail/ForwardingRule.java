package org.cmdbuild.services.scheduler.reademail;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.model.email.Email;

public abstract class ForwardingRule implements Rule {

	private final Rule delegate;

	protected ForwardingRule(final Rule delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean apply(final Email input) {
		return delegate.apply(input);
	}

	@Override
	public Email adapt(final Email email) {
		return delegate.adapt(email);
	}

	@Override
	public RuleAction action(final Email email) {
		return delegate.action(email);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("delegate", delegate) //
				.toString();
	}

}
