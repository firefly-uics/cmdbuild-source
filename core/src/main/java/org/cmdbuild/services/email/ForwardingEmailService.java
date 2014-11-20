package org.cmdbuild.services.email;

import java.net.URL;
import java.util.Map;

import org.cmdbuild.data.store.email.Email;
import org.cmdbuild.data.store.email.ExtendedEmailTemplate;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingEmailService extends ForwardingObject implements EmailService {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingEmailService() {
	}

	@Override
	protected abstract EmailService delegate();

	@Override
	public void send(final Email email) throws EmailServiceException {
		delegate().send(email);
	}

	@Override
	public void send(final Email email, final Map<URL, String> attachments) throws EmailServiceException {
		delegate().send(email, attachments);
	}

	@Override
	public void receive(final EmailCallbackHandler callback) throws EmailServiceException {
		delegate().receive(callback);
	}

	@Override
	public Iterable<Email> receive() throws EmailServiceException {
		return delegate().receive();
	}

	@Override
	public Iterable<ExtendedEmailTemplate> getEmailTemplates(final Email email) {
		return delegate().getEmailTemplates(email);
	}

	@Override
	public Long save(final Email email) {
		return delegate().save(email);
	}

	@Override
	public void delete(final Email email) {
		delegate().delete(email);
	}

	@Override
	public Iterable<Email> getEmails(final Long processId) {
		return delegate().getEmails(processId);
	}

	@Override
	public Iterable<Email> getOutgoingEmails(final Long processId) {
		return delegate().getOutgoingEmails(processId);
	}

}
