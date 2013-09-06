package org.cmdbuild.logic.email;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailConfigurationFactory;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.SubjectHandler;

public class EmailLogic implements Logic {

	private final EmailConfigurationFactory configurationFactory;
	private final EmailService service;
	private final SubjectHandler subjectHandler;
	private final Notifier notifier;

	public EmailLogic( //
			final EmailConfigurationFactory configurationFactory, //
			final EmailService service, //
			final SubjectHandler subjectHandler, //
			final Notifier notifier //
	) {
		this.configurationFactory = configurationFactory;
		this.service = service;
		this.subjectHandler = subjectHandler;
		this.notifier = notifier;
	}

	public Iterable<Email> getEmails(final Long processCardId) {
		return service.getEmails(processCardId);
	}

	public void sendOutgoingAndDraftEmails(final Long processCardId) {
		final EmailConfiguration configuration = configurationFactory.create();
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
