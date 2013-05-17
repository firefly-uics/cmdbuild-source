package org.cmdbuild.model;

import org.cmdbuild.data.store.Store.Storable;
import org.joda.time.DateTime;

public class Email extends AbstractEmail implements Storable {

	public enum EmailStatus {
		NEW("New"), //
		RECEIVED("Received"), //
		DRAFT("Draft"), //
		OUTGOING("Outgoing"), //
		SENT("Sent");

		private String lookupName;

		EmailStatus(final String lookupName) {
			this.lookupName = lookupName;
		}

		public String getLookupName() {
			return lookupName;
		}

		public static EmailStatus fromName(final String lookupName) {
			for (final EmailStatus status : EmailStatus.values()) {
				if (status.getLookupName().equals(lookupName)) {
					return status;
				}
			}
			throw new IllegalArgumentException();
		}
	}

	private final Long id;
	private String fromAddress;
	private DateTime date;
	private EmailStatus status;
	private Integer activityId;

	public Email() {
		this.id = null;
	}

	public Email(final long id) {
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

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(final Integer activityId) {
		this.activityId = activityId;
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}
}
