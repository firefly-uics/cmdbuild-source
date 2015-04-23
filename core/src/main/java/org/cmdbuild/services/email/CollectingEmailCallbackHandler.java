package org.cmdbuild.services.email;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

public class CollectingEmailCallbackHandler implements EmailCallbackHandler {

	private final Collection<Email> emails = newArrayList();

	@Override
	public void handle(final Email email) {
		emails.add(email);
	}

	public Iterable<Email> getEmails() {
		return emails;
	}

}
