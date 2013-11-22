package org.cmdbuild.logic.email;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static com.google.common.collect.Iterables.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailConfigurationFactory;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.SubjectHandler;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class EmailLogic implements Logic {

	private static final Function<Email, Long> EMAIL_ID_FUNCTION = new Function<Email, Long>() {

		@Override
		public Long apply(final Email input) {
			return input.getId();
		}

	};

	private static final Collection<EmailStatus> SAVEABLE_STATUSES = Arrays.asList(EmailStatus.DRAFT, null);

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

	/**
	 * Deletes all {@link Email}s with the specified id and for the specified
	 * process' id. Only draft mails can be deleted.
	 */
	public void deleteEmails(final Long processCardId, final List<Long> emailIds) {
		if (isEmpty(emailIds)) {
			return;
		}
		final Map<Long, Email> storedEmails = storedEmailsById(processCardId);
		for (final Long emailId : emailIds) {
			final Email found = storedEmails.get(emailId);
			Validate.notNull(found, "email not found");
			Validate.isTrue(SAVEABLE_STATUSES.contains(found.getStatus()), "specified email have no compatible status");
			service.delete(found);
		}
	}

	/**
	 * Saves all specified {@link Email}s for the specified process' id. Only
	 * draft mails can be saved, others are skipped.
	 */
	public void saveEmails(final Long processCardId, final Iterable<Email> emails) {
		if (isEmpty(emails)) {
			return;
		}
		final Map<Long, Email> storedEmails = storedEmailsById(processCardId);
		for (final Email email : emails) {
			final Email found = storedEmails.get(email.getId());
			final Email maybeUpdateable = (found == null) ? email : found;
			if (SAVEABLE_STATUSES.contains(maybeUpdateable.getStatus())) {
				maybeUpdateable.setActivityId(processCardId);
				service.save(maybeUpdateable);
			}
		}
	}

	private Map<Long, Email> storedEmailsById(final Long processCardId) {
		return Maps.uniqueIndex(service.getEmails(processCardId), EMAIL_ID_FUNCTION);
	}

}
