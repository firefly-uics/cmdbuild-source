package org.cmdbuild.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.wrappers.EmailCard;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.email.EmailService;
import org.joda.time.DateTime;

/**
 * Business Logic Layer for Email Management.
 * 
 * The API is still a work in progress.
 */
@Legacy("Legacy implementation")
public class EmailLogic {

	public enum EmailStatus {
		New,
		Received,
		Draft,
		Outgoing,
		Sent
	}

	public static class Email extends AbstractEmail {
		private Long id;
		private String fromAddress;
		private DateTime date;
		private EmailStatus status;

		public Email() {
			this.id = null;
		}

		public Email(long id) {
			this.id = id;
		}

		public Long getId() {
			return id;
		}

		public String getFromAddress() {
			return fromAddress;
		}

		public void setFromAddress(final String fromAddress) {
			this.fromAddress = fromAddress;
		}

		public DateTime getDate() {
			return date;
		}

		public void setDate(final DateTime date) {
			this.date = date;
		}

		public EmailStatus getStatus() {
			return status;
		}

		public void setStatus(final EmailStatus status) {
			this.status = status;
		}
	}

	public static class AbstractEmail {
		private String toAddresses;
		private String ccAddresses;
		private String subject;
		private String content;

		public String getToAddresses() {
			return toAddresses;
		}

		public void setToAddresses(String toAddresses) {
			this.toAddresses = toAddresses;
		}

		public String getCcAddresses() {
			return ccAddresses;
		}

		public void setCcAddresses(String ccAddresses) {
			this.ccAddresses = ccAddresses;
		}

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}

	private final UserContext userContext;

	public EmailLogic(final UserContext userContext) {
		this.userContext = userContext;
	}

	public void retrieveEmails() {
		try {
			EmailService.syncEmail();
		} catch (CMDBException e) {
			RequestListener.getCurrentRequest().pushWarning(e);
		} catch (IOException e) {
			throw new RuntimeException("Error rietrieving emails", e);
		}
	}

	public Iterable<Email> getEmails(final Long processCardId) {
		List<Email> emails = new ArrayList<Email>();
		final ICard processCard = fetchProcessCard(processCardId);
		for (final ICard card : EmailCard.list(processCard)) {
			final EmailCard emailCard = new EmailCard(card);
			final Email email = createFromCard(emailCard);
			emails.add(email);
		}
		return emails;
	}

	private ICard fetchProcessCard(final Long processCardId) {
		final ICard processCard = userContext.tables().get(ProcessType.BaseTable).cards().get(processCardId.intValue());
		return processCard;
	}

	private Email createFromCard(final EmailCard emailCard) {
		final Email email = new Email(emailCard.getId());
		email.setFromAddress(emailCard.getFrom());
		email.setToAddresses(emailCard.getTO());
		email.setCcAddresses(emailCard.getCC());
		email.setSubject(emailCard.getSubject());
		email.setContent(emailCard.getBody());

		email.setDate(new DateTime(emailCard.getBeginDate().getTime()));
		email.setStatus(EmailStatus.valueOf(emailCard.getEmailStatusDescription()));
		return email;
	}

	public void sendOutgoingAndDraftEmails(final Long processCardId) {
		final ICard processCard = fetchProcessCard(processCardId);
		EmailCard.sendOutgoingAndDrafts(processCard);
	}

	public void deleteEmail(final Long processCardId, final Long emailId) {
		final ICard processCard = fetchProcessCard(processCardId);
		final EmailCard emailCard = EmailCard.get(processCard, emailId.intValue());
		emailCard.delete();
	}

	public void saveEmail(final Long processCardId, final Email email) {
		final EmailCard emailCard = getOrCreateEmailCard(processCardId, email);
		copyEmailToCard(email, emailCard);
		emailCard.save();
	}

	private EmailCard getOrCreateEmailCard(final Long processCardId, final Email email) {
		final ICard processCard = fetchProcessCard(processCardId);
		final EmailCard emailCard;
		if (email.getId() == null) {
			emailCard = EmailCard.create(processCard);
			emailCard.setEmailStatus(EmailCard.EmailStatus.DRAFT);
		} else {
			final int emailCardId = email.getId().intValue();
			emailCard = EmailCard.get(processCard, emailCardId);
		}
		return emailCard;
	}

	private void copyEmailToCard(final Email email, final EmailCard emailCard) {
		emailCard.setTO(email.getToAddresses());
		emailCard.setCC(email.getCcAddresses());
		emailCard.setSubject(email.getSubject());
		emailCard.setBody(email.getContent());
	}
}
