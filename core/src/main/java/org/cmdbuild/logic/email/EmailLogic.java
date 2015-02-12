package org.cmdbuild.logic.email;

import org.cmdbuild.data.store.email.EmailStatus;
import org.cmdbuild.logic.Logic;
import org.joda.time.DateTime;

public interface EmailLogic extends Logic {

	static interface Email {

		Long getId();

		String getFromAddress();

		String getToAddresses();

		String getCcAddresses();

		String getBccAddresses();

		String getSubject();

		String getContent();

		DateTime getDate();

		EmailStatus getStatus();

		Long getActivityId();

		String getNotifyWith();

		boolean isNoSubjectPrefix();

		String getAccount();

		boolean isTemporary();

		String getTemplate();

	}

	public static class EmailSubmission extends org.cmdbuild.data.store.email.Email {

		private String temporaryId;

		public EmailSubmission() {
			super();
		}

		public EmailSubmission(final long id) {
			super(id);
		}

		public String getTemporaryId() {
			return temporaryId;
		}

		public void setTemporaryId(final String temporaryId) {
			this.temporaryId = temporaryId;
		}

	}

	Long create(Email email);

	void update(Email email);

	Email read(Email email);

	void delete(Email email);

	Iterable<Email> getEmails(Long processCardId);

	void sendOutgoingAndDraftEmails(Long processCardId);

	/**
	 * Deletes all {@link Email}s with the specified id and for the specified
	 * process' id. Only draft mails can be deleted.
	 */
	void deleteEmails(Long processCardId, Iterable<Long> emailIds);

	/**
	 * Saves all specified {@link EmailSubmission}s for the specified process'
	 * id. Only draft mails can be saved, others are skipped.
	 */
	void saveEmails(Long processCardId, Iterable<? extends EmailSubmission> emails);

}
