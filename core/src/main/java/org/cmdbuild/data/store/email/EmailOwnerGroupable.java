package org.cmdbuild.data.store.email;

import static org.cmdbuild.data.store.Groupables.nameAndValue;
import static org.cmdbuild.data.store.email.EmailConstants.CARD_ATTRIBUTE;

import org.cmdbuild.data.store.ForwardingGroupable;
import org.cmdbuild.data.store.Groupable;

public class EmailOwnerGroupable extends ForwardingGroupable {

	public static EmailOwnerGroupable of(final Long owner) {
		return new EmailOwnerGroupable(nameAndValue(CARD_ATTRIBUTE, owner));
	}

	private final Groupable delegate;

	private EmailOwnerGroupable(final Groupable delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Groupable delegate() {
		return delegate;
	}

}