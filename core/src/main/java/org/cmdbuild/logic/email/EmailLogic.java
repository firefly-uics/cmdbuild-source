package org.cmdbuild.logic.email;

import org.cmdbuild.data.store.email.EmailStatus;
import org.cmdbuild.logic.Logic;
import org.joda.time.DateTime;

import com.google.common.collect.ForwardingObject;

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

	static abstract class ForwardingEmail extends ForwardingObject implements Email {

		@Override
		protected abstract Email delegate();

		@Override
		public Long getId() {
			return delegate().getId();
		}

		@Override
		public String getFromAddress() {
			return delegate().getFromAddress();
		}

		@Override
		public String getToAddresses() {
			return delegate().getToAddresses();
		}

		@Override
		public String getCcAddresses() {
			return delegate().getCcAddresses();
		}

		@Override
		public String getBccAddresses() {
			return delegate().getBccAddresses();
		}

		@Override
		public String getSubject() {
			return delegate().getSubject();
		}

		@Override
		public String getContent() {
			return delegate().getContent();
		}

		@Override
		public DateTime getDate() {
			return delegate().getDate();
		}

		@Override
		public EmailStatus getStatus() {
			return delegate().getStatus();
		}

		@Override
		public Long getActivityId() {
			return delegate().getActivityId();
		}

		@Override
		public String getNotifyWith() {
			return delegate().getNotifyWith();
		}

		@Override
		public boolean isNoSubjectPrefix() {
			return delegate().isNoSubjectPrefix();
		}

		@Override
		public String getAccount() {
			return delegate().getAccount();
		}

		@Override
		public boolean isTemporary() {
			return delegate().isTemporary();
		}

		@Override
		public String getTemplate() {
			return delegate().getTemplate();
		}

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

	Iterable<Email> readAll(Long processCardId);

	Email read(Email email);

	void update(Email email);

	void delete(Email email);

	void send(Email email);

}
