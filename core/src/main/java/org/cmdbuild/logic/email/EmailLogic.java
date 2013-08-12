package org.cmdbuild.logic.email;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.DefaultEmailCallbackHandler;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailCallbackHandler.RuleAction;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.SubjectHandler;

public class EmailLogic implements Logic {

	private final EmailConfiguration configuration;
	private final EmailService service;
	private final Rule rule;
	private final SubjectHandler subjectHandler;
	private final Notifier notifier;

	public EmailLogic( //
			final EmailConfiguration configuration, //
			final EmailService service,//
			final Rule rule,//
			final SubjectHandler subjectHandler, //
			final Notifier notifier //
	) {
		this.configuration = configuration;
		this.service = service;
		this.rule = rule;
		this.subjectHandler = subjectHandler;
		this.notifier = notifier;
	}

	public Iterable<Email> getEmails(final Long processCardId) {
		return service.getEmails(processCardId);
	}

	// TODO move in another component
	public void retrieveEmailsFromServer() {
		try {
			final DefaultEmailCallbackHandler callbackHandler = DefaultEmailCallbackHandler.of(rule);
			service.receive(callbackHandler);

			logger.info("executing actions");
			for (final RuleAction action : callbackHandler.getActions()) {
				try {
					action.execute();
				} catch (final Exception e) {
					logger.warn("error executing action");
				}
			}
		} catch (final CMDBException e) {
			notifier.warn(e);
		}
	}

	public void sendOutgoingAndDraftEmails(final Long processCardId) {
		for (final Email email : service.getOutgoingEmails(processCardId)) {
			email.setFromAddress(configuration.getEmailAddress());
			try {
				email.setSubject(defaultIfBlank(subjectHandler.compile(email).getSubject(), EMPTY));
				service.send(email);
				email.setStatus(EmailStatus.SENT);
			} catch (final CMDBException ex) {
				notifier.warn(ex);
				email.setStatus(EmailStatus.OUTGOING);
			}
			service.save(email);
		}
	}

	public void deleteEmail(final Long processCardId, final Long emailId) {
		final Email email = new Email(emailId);
		email.setActivityId(processCardId);
		service.delete(email);
	}

	public void saveEmail(final Long processCardId, final Email email) {
		email.setActivityId(processCardId);
		service.save(email);
	}

}
