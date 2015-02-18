package org.cmdbuild.data.store.email;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.model.AbstractEmail;
import org.joda.time.DateTime;

public class Email extends AbstractEmail implements Storable {

	private static Iterable<Attachment> NO_ATTACHMENTS = emptyList();

	private final Long id;
	private DateTime date;
	private EmailStatus status;
	private Long activityId;
	private Iterable<Attachment> attachments;
	private boolean noSubjectPrefix;
	private String account;
	private String template;
	private boolean keepSynchronization;
	private boolean promptSynchronization;

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
		return defaultIfNull(attachments, NO_ATTACHMENTS);
	}

	public void setAttachments(final Iterable<Attachment> attachments) {
		this.attachments = attachments;
	}

	public boolean isNoSubjectPrefix() {
		return noSubjectPrefix;
	}

	public void setNoSubjectPrefix(final boolean noSubjectPrefix) {
		this.noSubjectPrefix = noSubjectPrefix;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(final String account) {
		this.account = account;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(final String template) {
		this.template = template;
	}

	public boolean isKeepSynchronization() {
		return keepSynchronization;
	}

	public void setKeepSynchronization(final boolean keepSynchronization) {
		this.keepSynchronization = keepSynchronization;
	}

	public boolean isPromptSynchronization() {
		return promptSynchronization;
	}

	public void setPromptSynchronization(final boolean promptSynchronization) {
		this.promptSynchronization = promptSynchronization;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Email)) {
			return false;
		}

		final Email other = Email.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.getFromAddress(), other.getFromAddress()) //
				.append(this.getToAddresses(), other.getToAddresses()) //
				.append(this.getCcAddresses(), other.getCcAddresses()) //
				.append(this.getBccAddresses(), other.getBccAddresses()) //
				.append(this.getSubject(), other.getSubject()) //
				.append(this.getContent(), other.getContent()) //
				.append(this.getDate(), other.getDate()) //
				.append(this.getStatus(), other.getStatus()) //
				.append(this.getActivityId(), other.getActivityId()) //
				.append(this.getNotifyWith(), other.getNotifyWith()) //
				.append(this.isNoSubjectPrefix(), other.isNoSubjectPrefix()) //
				.append(this.getAccount(), other.getAccount()) //
				.append(this.getTemplate(), other.getTemplate()) //
				.append(this.isKeepSynchronization(), other.isKeepSynchronization()) //
				.append(this.isPromptSynchronization(), other.isPromptSynchronization()) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(this.getId()) //
				.append(this.getFromAddress()) //
				.append(this.getToAddresses()) //
				.append(this.getCcAddresses()) //
				.append(this.getBccAddresses()) //
				.append(this.getSubject()) //
				.append(this.getContent()) //
				.append(this.getDate()) //
				.append(this.getStatus()) //
				.append(this.getActivityId()) //
				.append(this.getNotifyWith()) //
				.append(this.isNoSubjectPrefix()) //
				.append(this.getAccount()) //
				.append(this.getTemplate()) //
				.append(this.isKeepSynchronization()) //
				.append(this.isPromptSynchronization()) //
				.toHashCode();
	}

	@Override
	public final String toString() {
		return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE).toString();
	}

}
