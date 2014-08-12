package org.cmdbuild.data.store.email;

import java.util.Collections;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.model.AbstractEmail;
import org.joda.time.DateTime;

public class Email extends AbstractEmail implements Storable {

	private final Long id;
	private String fromAddress;
	private DateTime date;
	private EmailStatus status;
	private Long activityId;
	private Iterable<Attachment> attachments;
	private String account;

	public Email() {
		this.id = null;
	}

	public Email(final long id) {
		this.id = id;
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	public Long getId() {
		return id;
	}

	@Override
	public String getFromAddress() {
		return fromAddress;
	}

	@Override
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

	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(final Long activityId) {
		this.activityId = activityId;
	}

	public Iterable<Attachment> getAttachments() {
		return (attachments == null) ? Collections.<Attachment> emptyList() : attachments;
	}

	public void setAttachments(final Iterable<Attachment> attachments) {
		this.attachments = attachments;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(final String account) {
		this.account = account;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
