package org.cmdbuild.services.email;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailCallbackHandler.RuleAction;

public abstract class ForwardingRule implements Rule {

	private final Rule inner;

	protected ForwardingRule(final Rule rule) {
		this.inner = rule;
	}

	@Override
	public boolean applies(final Email email) {
		return inner.applies(email);
	}

	@Override
	public Email adapt(final Email email) {
		return inner.adapt(email);
	}

	@Override
	public RuleAction action(final Email email) {
		return inner.action(email);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("rule", inner) //
				.toString();
	}

}
