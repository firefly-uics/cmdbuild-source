package org.cmdbuild.logic.email.rules;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.EmailConstants;
import org.cmdbuild.model.email.EmailTemplate;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailCallbackHandler.RuleAction;
import org.cmdbuild.services.email.EmailPersistence;
import org.cmdbuild.services.email.EmailRecipientTemplateResolver;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.SubjectHandler;
import org.cmdbuild.services.email.SubjectHandler.ParsedSubject;
import org.cmdbuild.spring.annotations.LogicComponent;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@LogicComponent
public class AnswerToExistingMail implements Rule {

	private static final Logger logger = Logic.logger;

	private final EmailService service;
	private final EmailPersistence persistence;
	private final SubjectHandler subjectHandler;
	private final EmailRecipientTemplateResolver templateResolver;

	private ParsedSubject parsedSubject;
	private Email parentEmail;

	@Autowired
	public AnswerToExistingMail( //
			final EmailService service, //
			final EmailPersistence persistence, //
			final SubjectHandler subjectHandler, //
			final EmailRecipientTemplateResolver templateResolver //
	) {
		this.service = service;
		this.persistence = persistence;
		this.subjectHandler = subjectHandler;
		this.templateResolver = templateResolver;
	}

	@Override
	public boolean applies(final Email email) {
		parsedSubject = subjectHandler.parse(email.getSubject());
		if (!parsedSubject.hasExpectedFormat()) {
			return false;
		}

		try {
			parentEmail = persistence.getEmail(parsedSubject.getEmailId());
		} catch (final Exception e) {
			return false;
		}

		return true;
	}

	@Override
	public Email adapt(final Email email) {
		email.setSubject(parsedSubject.getRealSubject());
		email.setActivityId(parentEmail.getActivityId());
		email.setNotifyWith(parentEmail.getNotifyWith());
		return email;
	}

	@Override
	public RuleAction action(final Email email) {
		return new RuleAction() {

			@Override
			public void execute() {
				sendNotificationFor(email);
			}

			private void sendNotificationFor(final Email email) {
				logger.debug("sending notification for email with id '{}'", email.getId());
				try {
					for (final EmailTemplate emailTemplate : service.getEmailTemplates(email)) {
						final Email notification = resolve(emailTemplate);
						service.send(notification);
					}
				} catch (final Exception e) {
					logger.warn("error sending notification", e);
				}
			}

			private Email resolve(final EmailTemplate emailTemplate) {
				final Email email = new Email();
				email.setToAddresses(resolveRecipients(emailTemplate.getToAddresses()));
				email.setCcAddresses(resolveRecipients(emailTemplate.getCCAddresses()));
				email.setBccAddresses(resolveRecipients(emailTemplate.getBCCAddresses()));
				email.setSubject(emailTemplate.getSubject());
				email.setContent(emailTemplate.getBody());
				return email;
			}

			private String resolveRecipients(final Iterable<String> recipients) {
				return StringUtils.join(templateResolver.resolve(recipients).iterator(),
						EmailConstants.ADDRESSES_SEPARATOR);
			}

		};
	}

}