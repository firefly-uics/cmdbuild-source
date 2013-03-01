package org.cmdbuild.elements.wrappers;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.config.EmailProperties;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.Reference;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.email.EmailService;

@Deprecated
public class EmailCard extends LazyCard {

	private static final long serialVersionUID = 1L;

	public static final String EMAIL_CLASS_NAME = "Email";
	private static final ITable emailClass = UserOperations.from(UserContext.systemContext()).tables()
			.get(EMAIL_CLASS_NAME);

	public static final String FromAttr = "FromAddress";
	public static final String TOAttr = "ToAddresses";
	public static final String CCAttr = "CcAddresses";
	public static final String SubjectAttr = "Subject";
	public static final String BodyAttr = "Content";

	public static final String ActivityAttr = "Activity";
	public static final String EmailStatusAttr = "EmailStatus";
	public static final String EmailStatusLookupType = "EmailStatus";

	public enum EmailStatus {
		NEW("New", true), RECEIVED("Received", true), DRAFT("Draft", false), OUTGOING("Outgoing", false), SENT("Sent",
				false);
		private String lookupName;
		private boolean received;

		EmailStatus(final String lookupName, final boolean received) {
			this.lookupName = lookupName;
			this.received = received;
		}

		static EmailStatus fromName(final String lookupName) {
			for (final EmailStatus status : EmailStatus.values()) {
				if (status.lookupName.equals(lookupName))
					return status;
			}
			throw new IllegalArgumentException();
		}

		static EmailStatus fromLookup(final Lookup lookup) {
			return fromName(lookup.getDescription());
		}

		public boolean isReceived() {
			return received;
		}

		public Lookup getLookup() {
			// FIXME don't use the business layer, but refactor the lookups
			final LookupOperation lo = new LookupOperation(UserContext.systemContext());
			return lo.getLookup(EmailStatusLookupType, this.lookupName);
		}
	}

	public EmailCard() throws NotFoundException {
		super(emailClass.cards().create());
	}

	public EmailCard(final ICard card) throws NotFoundException {
		super(card);
	}

	public EmailCard(final Message message) throws MessagingException, IOException {
		super(emailClass.cards().create());
		parseMessage(message);
	}

	public static EmailCard create(final ICard processCard) {
		final EmailCard newEmail = new EmailCard(UserOperations.from(UserContext.systemContext()).tables()
				.get(EMAIL_CLASS_NAME).cards().create());
		newEmail.setActivity(processCard);
		return newEmail;
	}

	/*
	 * Needed because the Email class is reserved
	 */
	@OldDao
	public static CardQuery list(final ICard processCard) {
		return UserOperations.from(UserContext.systemContext()).tables().get(EMAIL_CLASS_NAME).cards().list()
				.filter(EmailCard.ActivityAttr, AttributeFilterType.EQUALS, String.valueOf(processCard.getId()));
	}

	@OldDao
	public static EmailCard get(final ICard processCard, final int emailCardId) {
		final ICard emailCard = EmailCard.list(processCard).id(emailCardId).get();
		return new EmailCard(emailCard);
	}

	static public void sendOutgoingAndDrafts(final ICard processCard) {
		final Iterable<ICard> outgoingCards = listOutgoingAndDrafts(processCard);
		for (final ICard outgoingCard : outgoingCards) {
			final EmailCard outgoing = new EmailCard(outgoingCard);
			outgoing.sendAndSave();
		}
	}

	private void sendAndSave() {
		setWorkflowAddress();
		try {
			EmailService.sendEmail(this);
			setEmailStatus(EmailStatus.SENT);
		} catch (final CMDBException e) {
			RequestListener.getCurrentRequest().pushWarning(e);
			setEmailStatus(EmailStatus.OUTGOING);
		}
		save();
	}

	private void setWorkflowAddress() {
		final String wfAddress = EmailProperties.getInstance().getEmailAddress();
		setFrom(wfAddress);
	}

	private static CardQuery listOutgoingAndDrafts(final ICard processCard) {
		return list(processCard).filter(EmailStatusAttr, AttributeFilterType.IN,
				String.valueOf(EmailStatus.OUTGOING.getLookup().getId()),
				String.valueOf(EmailStatus.DRAFT.getLookup().getId()));
	}

	private void parseMessage(final Message message) throws MessagingException, IOException {
		final String fromHeader = extractFrom(message);
		setMessageStatusFromSender(fromHeader);
		setTO(extractTO(message));
		setCC(extractCC(message));
		setSubject(extractSubject(message));
		setBody(extractBody(message));
		setActivity(extractActivity(message));
	}

	private void setMessageStatusFromSender(final String fromHeader) throws AddressException {
		final InternetAddress emailFromAddress = new InternetAddress(fromHeader);
		final InternetAddress wfFromAddress = new InternetAddress(EmailProperties.getInstance().getEmailAddress());
		setFrom(fromHeader);
		if (emailFromAddress.getAddress().equalsIgnoreCase(wfFromAddress.getAddress())) {
			// Probably sent from Shark with BCC here
			setEmailStatus(EmailStatus.SENT);
		} else {
			setEmailStatus(EmailStatus.RECEIVED); // TODO Set as NEW!
		}
	}

	static private String extractFrom(final Message message) throws MessagingException {
		final String[] fromHeaders = message.getHeader("From");
		if (fromHeaders != null && fromHeaders.length > 0) {
			return fromHeaders[0];
		} else {
			return "";
		}
	}

	static private String extractTO(final Message message) throws MessagingException {
		final String[] toHeaders = message.getHeader("TO");
		if (toHeaders != null && toHeaders.length > 0) {
			return toHeaders[0];
		} else {
			return "";
		}
	}

	static private String extractCC(final Message message) throws MessagingException {
		final String[] ccHeaders = message.getHeader("CC");
		if (ccHeaders != null && ccHeaders.length > 0) {
			return ccHeaders[0];
		} else {
			return "";
		}
	}

	static private String extractSubject(final Message message) throws MessagingException {
		final String emailSubject = message.getSubject();
		if (emailSubject == null) {
			throw new IllegalArgumentException();
		}
		final int activitySectionEnd = emailSubject.indexOf("]");
		if (activitySectionEnd < 0) {
			throw new IllegalArgumentException();
		}
		return emailSubject.substring(activitySectionEnd + 1).trim();
	}

	static private String extractBody(final Message message) throws MessagingException, IOException {
		final Object messageContent = message.getContent();
		if (messageContent == null) {
			throw new IllegalArgumentException();
		}
		if (messageContent instanceof Multipart) {
			final Multipart mp = (Multipart) messageContent;
			for (int i = 0, n = mp.getCount(); i < n; ++i) {
				final Part part = mp.getBodyPart(i);
				final String disposition = part.getDisposition();
				if (disposition == null)
					return part.getContent().toString();
			}
			return "";
		}
		return messageContent.toString();
	}

	static private ICard extractActivity(final Message message) throws MessagingException {
		final String emailSubject = message.getSubject();
		final Pattern activityExtractor = Pattern.compile("[^\\[]*\\[(\\S+)\\s+(\\d+)\\]");
		final Matcher activityParts = activityExtractor.matcher(emailSubject);
		if (!activityParts.lookingAt())
			throw new IllegalArgumentException();
		final String activityClassName = activityParts.group(1);
		final int activityId = Integer.parseInt(activityParts.group(2));
		try {
			final ICard activity = UserOperations.from(UserContext.systemContext()).tables().get(activityClassName)
					.cards().get(activityId);
			return activity;
		} catch (final NotFoundException e) {
		}
		throw new IllegalArgumentException();
	}

	public String getFrom() {
		return getAttributeValue(FromAttr).getString();
	}

	public void setFrom(final String address) {
		getAttributeValue(FromAttr).setValue(address);
	}

	public String getTO() {
		return getAttributeValue(TOAttr).getString();
	}

	public void setTO(final String address) {
		getAttributeValue(TOAttr).setValue(address);
	}

	public String getCC() {
		return getAttributeValue(CCAttr).getString();
	}

	public void setCC(final String cc) {
		getAttributeValue(CCAttr).setValue(cc);
	}

	public String getSubject() {
		return getAttributeValue(SubjectAttr).getString();
	}

	public void setSubject(final String subject) {
		getAttributeValue(SubjectAttr).setValue(subject);
	}

	public String getBody() {
		return getAttributeValue(BodyAttr).getString();
	}

	public void setBody(final String body) {
		getAttributeValue(BodyAttr).setValue(body);
	}

	private void setActivity(final ICard activity) {
		final AttributeValue av = getAttributeValue(ActivityAttr);
		av.setValue(new Reference(av.getSchema().getReferenceDirectedDomain(), activity.getId(), activity
				.getDescription()));
	}

	private ICard getActivity() {
		final Reference activityReference = getAttributeValue(ActivityAttr).getReference();
		return new LazyCard(activityReference.getClassId(), activityReference.getId());
	}

	public int getActivityId() {
		return getActivity().getId();
	}

	public String getActivityName() {
		return getActivity().getSchema().getName();
	}

	public void setEmailStatus(final EmailStatus status) {
		getAttributeValue(EmailStatusAttr).setValue(status.getLookup());
	}

	public void setEmailStatus(final String status) {
		setEmailStatus(EmailStatus.fromName(status));
	}

	public String getEmailStatusDescription() {
		final Lookup statusLookup = getAttributeValue(EmailStatusAttr).getLookup();
		return statusLookup.getDescription();
	}
}
