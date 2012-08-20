package org.cmdbuild.shark.toolagent;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.split;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.common.mail.NewMail;
import org.cmdbuild.workflow.ConfigurationHelper;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class SafeMailToolAgent extends AbstractConditionalToolAgent {

	private static final String FROM_ADDRESS = "FromAddress";
	private static final String FROM_ADDRESSES = "FromAddresses";
	private static final String TO_ADDRESSES = "ToAddresses";
	private static final String CC_ADDRESSES = "CcAddresses";
	private static final String BCC_ADDRESSES = "BccAddresses";
	private static final String SUBJECT = "Subject";
	private static final String CONTENT = "Content";
	private static final String MIME_TYPE = "MimeType";
	private static final String URL_ATTACHMENTS = "UrlAttachments";

	private static final String COMMA_SEPARATOR = ",";

	private MailApi mailApi;

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);

		final ConfigurationHelper helper = new ConfigurationHelper(cus);
		final MailApiFactory mailApiFactory = helper.getMailApiFactory();
		final MailApi.Configuration cusConfiguration = helper.getMailApiConfiguration();
		mailApiFactory.setConfiguration(cusConfiguration);
		mailApi = mailApiFactory.createMailApi();
	}

	@Override
	protected void innerInvoke() throws Exception {

		final NewMail newMail = mailApi.newMail();
		for (final String fromAddress : fromAddresses()) {
			newMail.withFrom(fromAddress);
		}
		for (final String ccAddress : toAddresses()) {
			newMail.withTo(ccAddress);
		}
		for (final String ccAddress : ccAddresses()) {
			newMail.withCc(ccAddress);
		}
		for (final String bccAddress : bccAddresses()) {
			newMail.withBcc(bccAddress);
		}
		newMail //
		.withSubject(subject()) //
				.withContent(content()) //
				.withContentType(contentType());
		for (final String urlAttachment : urlAttachments()) {
			final URL url = new URL(urlAttachment);
			newMail.withAttachment(url);
		}
		newMail.send();
	}

	private List<String> fromAddresses() {
		return listFromParameters(FROM_ADDRESS, FROM_ADDRESSES);
	}

	private List<String> toAddresses() {
		return listFromParameters(TO_ADDRESSES);
	}

	private List<String> ccAddresses() {
		return listFromParameters(CC_ADDRESSES);
	}

	private List<String> bccAddresses() {
		return listFromParameters(BCC_ADDRESSES);
	}

	private String subject() {
		return getSafeParameterValue(SUBJECT);
	}

	private String content() {
		return getSafeParameterValue(CONTENT);
	}

	private String contentType() {
		return getSafeParameterValue(MIME_TYPE);
	}

	private List<String> urlAttachments() {
		return listFromParameters(URL_ATTACHMENTS);
	}

	/**
	 * Returns a list containing the elements of the first valid parameter. A
	 * valid parameter is present and have a non-null, non-empty, non-blank
	 * value. The list can be empty if no valid values are found. The value of
	 * the parameter is splitted using the {@code COMMA_SEPARATOR} string.
	 * 
	 * @param name
	 *            is the name of the mandatory parameter.
	 * @param otherNames
	 *            are the names of other optional parameters.
	 * 
	 * @return a list (never null) with the elements specified in the value of
	 *         the first valid parameter.
	 */
	private List<String> listFromParameters(final String name, final String... otherNames) {
		final List<String> parameterNames = new ArrayList<String>();
		parameterNames.add(name);
		parameterNames.addAll(asList(otherNames));

		for (final String parameterName : parameterNames) {
			if (hasParameter(parameterName)) {
				final String value = getParameterValue(parameterName);
				if (isNotBlank(value)) {
					return asList(split(value, COMMA_SEPARATOR));
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Returns an empty string if the parameter is missing, null, empty or
	 * blank.
	 * 
	 * @return an empty string if the parameter is missing, null, empty or
	 *         blank.
	 */
	private String getSafeParameterValue(final String name) {
		final String value;
		if (hasParameter(name)) {
			final String unsafeValue = getParameterValue(name);
			value = isNotBlank(unsafeValue) ? unsafeValue : EMPTY;
		} else {
			value = EMPTY;
		}
		return value;
	}

}
