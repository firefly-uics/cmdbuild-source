package org.cmdbuild.data.store.email;

import static org.cmdbuild.data.store.Groupables.nameAndValue;
import static org.cmdbuild.data.store.email.EmailConstants.PROCESS_ID_ATTRIBUTE;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.ForwardingGroupable;
import org.cmdbuild.data.store.Groupable;

public class EmailOwnerGroupable extends ForwardingGroupable {

	public static EmailOwnerGroupable of(final Long owner) {
		Validate.notNull(owner, "owner's id cannot be null");
		Validate.isTrue(owner > 0, "owner's id must be greater than zero");
		return new EmailOwnerGroupable(nameAndValue(PROCESS_ID_ATTRIBUTE, owner));
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