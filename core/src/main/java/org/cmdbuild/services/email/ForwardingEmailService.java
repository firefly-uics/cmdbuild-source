package org.cmdbuild.services.email;

import java.net.URL;
import java.util.Map;

import org.cmdbuild.data.store.email.Email;

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

}
