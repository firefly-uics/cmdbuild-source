package org.cmdbuild.logic.scheduler;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.services.email.EmailCallbackHandler.Applicable;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.ForwardingRule;

public class RuleWithAdditionalCondition extends ForwardingRule {

	private final Applicable applicable;

	public RuleWithAdditionalCondition(final Rule rule, final Applicable applicable) {
		super(rule);
		this.applicable = applicable;
	}

	@Override
	public boolean applies(final Email email) {
		return applicable.applies(email) ? super.applies(email) : false;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("condition", applicable) //
				.toString();
	}

}
