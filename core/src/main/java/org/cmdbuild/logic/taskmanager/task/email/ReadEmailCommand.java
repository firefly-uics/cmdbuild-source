package org.cmdbuild.logic.taskmanager.task.email;

import java.util.Collection;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.email.CollectingEmailCallbackHandler;
import org.cmdbuild.services.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

class ReadEmailCommand implements Command {

	private static final Logger logger = Log.EMAIL;
	private static Marker marker = MarkerFactory.getMarker(ReadEmailCommand.class.getName());

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ReadEmailCommand> {

		private EmailService emailService;
		private Predicate<Email> predicate;
		private final Collection<Triple<Predicate<Email>, Function<Email, Email>, Action>> triples = Lists
				.newArrayList();

		private Builder() {
			// use factory method
		}

		@Override
		public ReadEmailCommand build() {
			validate();
			return new ReadEmailCommand(this);
		}

		private void validate() {
			Validate.notNull(emailService, "invalid email service");
			Validate.notNull(predicate, "invalid predicate");
		}

		public ReadEmailCommand.Builder withEmailService(final EmailService emailService) {
			this.emailService = emailService;
			return this;
		}

		public ReadEmailCommand.Builder withPredicate(final Predicate<Email> predicate) {
			this.predicate = predicate;
			return this;
		}

		public ReadEmailCommand.Builder withAction(final Predicate<Email> predicate,
				final Function<Email, Email> function, final Action action) {
			this.triples.add(ImmutableTriple.of(predicate, function, action));
			return this;
		}

	}

	public static ReadEmailCommand.Builder newInstance() {
		return new Builder();
	}

	private final EmailService service;
	private final Predicate<Email> predicate;
	private final Iterable<Triple<Predicate<Email>, Function<Email, Email>, Action>> triples;

	private ReadEmailCommand(final ReadEmailCommand.Builder builder) {
		this.service = builder.emailService;
		this.predicate = builder.predicate;
		this.triples = builder.triples;
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
			for (final Triple<Predicate<Email>, Function<Email, Email>, Action> triple : triples) {
				if (triple.getLeft().apply(email)) {
					final Email adapted = triple.getMiddle().apply(email);
					service.save(adapted);
					triple.getRight().execute(adapted);
				}
			}
		}
		logger.info(marker, "finishing synchronization job");
	}

}