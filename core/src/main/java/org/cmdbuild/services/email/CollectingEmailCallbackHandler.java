package org.cmdbuild.services.email;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collection;

import org.cmdbuild.data.store.email.Email;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class CollectingEmailCallbackHandler implements EmailCallbackHandler {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<CollectingEmailCallbackHandler> {

		private static final Predicate<Email> ALWAYS_TRUE = alwaysTrue();

		private Predicate<Email> predicate;

		private Builder() {
			// use factory method
		}

		@Override
		public CollectingEmailCallbackHandler build() {
			validate();
			return new CollectingEmailCallbackHandler(this);
		}

		private void validate() {
			predicate = defaultIfNull(predicate, ALWAYS_TRUE);
		}

		public Builder withPredicate(final Predicate<Email> predicate) {
			this.predicate = predicate;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Predicate<Email> predicate;
	private final Collection<Email> emails;

	private CollectingEmailCallbackHandler(final Builder builder) {
		this.predicate = builder.predicate;
		this.emails = Lists.newArrayList();
	}

	@Override
	public void handle(final Email email) {
		emails.add(email);
	}

	public Iterable<Email> getEmails() {
		return from(emails) //
				.filter(predicate);
	}

}
