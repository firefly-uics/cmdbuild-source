package org.cmdbuild.logic.email;

import static java.util.Collections.emptyList;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.email.EmailAttachmentsLogic.ForwardingEmailAttachmentsLogic;
import org.cmdbuild.logic.email.EmailLogic.Email;

public class ConfigurationAwareEmailAttachmentsLogic extends ForwardingEmailAttachmentsLogic {

	private final EmailAttachmentsLogic delegate;
	private final DmsConfiguration configuration;

	public ConfigurationAwareEmailAttachmentsLogic(final EmailAttachmentsLogic delegate,
			final DmsConfiguration configuration) {
		this.delegate = delegate;
		this.configuration = configuration;
	}

	private static final EmailAttachmentsLogic UNSUPPORTED = UnsupportedProxyFactory.of(EmailAttachmentsLogic.class)
			.create();
	private static final Iterable<Attachment> NO_ATTACHMENTS = emptyList();

	@Override
	protected EmailAttachmentsLogic delegate() {
		return configuration.isEnabled() ? delegate : UNSUPPORTED;
	}

	@Override
	public void copy(final Email email, final Attachment attachment) throws CMDBException {
		if (configuration.isEnabled()) {
			super.copy(email, attachment);
		}
	}

	@Override
	public void copyAll(final Email source, final Email destination) throws CMDBException {
		if (configuration.isEnabled()) {
			super.copyAll(source, destination);
		}
	}

	@Override
	public Iterable<Attachment> readAll(final Email email) throws CMDBException {
		return configuration.isEnabled() ? super.readAll(email) : NO_ATTACHMENTS;
	}

}