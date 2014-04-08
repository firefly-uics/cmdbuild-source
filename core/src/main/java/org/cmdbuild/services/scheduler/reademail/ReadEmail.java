package org.cmdbuild.services.scheduler.reademail;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.services.email.CollectingEmailCallbackHandler;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.scheduler.Command;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ReadEmail implements Command {

	private static final Logger logger = Log.EMAIL;
	private static Marker marker = MarkerFactory.getMarker(ReadEmail.class.getName());

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ReadEmail> {

		private static final Iterable<Rule> EMPTY_RULES = Collections.emptyList();

		private EmailService emailService;
		private Predicate<Email> predicate;
		private final Collection<Rule> rules = Lists.newArrayList();

		private Builder() {
			// use factory method
		}

		@Override
		public ReadEmail build() {
			validate();
			return new ReadEmail(this);
		}

		private void validate() {
			Validate.notNull(emailService, "invalid email service");
			Validate.notNull(predicate, "invalid predicate");
		}

		public Builder withEmailService(final EmailService emailService) {
			this.emailService = emailService;
			return this;
		}
		
		public Builder withPredicate(final Predicate<Email> predicate) {
			this.predicate = predicate;
			return this;
		}

		public Builder withRules(final Iterable<Rule> rules) {
			Iterables.addAll(this.rules, defaultIfNull(rules, EMPTY_RULES));
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final EmailService service;
	private final Predicate<Email> predicate;
	private final Iterable<Rule> rules;

	private ReadEmail(final Builder builder) {
		this.service = builder.emailService;
		this.predicate = builder.predicate;
		this.rules = builder.rules;
	}

	@Override
	public void execute() {
		logger.info(marker, "starting synchronization job");
		final CollectingEmailCallbackHandler callbackHandler = CollectingEmailCallbackHandler.newInstance() //
				.withPredicate(predicate) //
				.build();
		service.receive(callbackHandler);

		logger.info(marker, "executing actions");
		for (final Email email : callbackHandler.getEmails()) {
			for (final Rule rule : rules) {
				if (rule.apply(email)) {
					final Email adapted = rule.adapt(email);
					service.save(adapted);
					rule.action(adapted).execute();
				}
			}
		}
		logger.info(marker, "finishing synchronization job");
	}

}
