package org.cmdbuild.common.api.mail.javax.mail;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.net.URL;
import java.util.Collection;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.cmdbuild.common.api.mail.Configuration.Output;
import org.cmdbuild.common.api.mail.ForwardingNewMail;
import org.cmdbuild.common.api.mail.MailException;
import org.cmdbuild.common.api.mail.NewMail;
import org.cmdbuild.common.api.mail.NewMailQueue;
import org.cmdbuild.common.api.mail.QueueableNewMail;
import org.cmdbuild.common.api.mail.javax.mail.OutputTemplate.Hook;
import org.slf4j.Logger;

import com.google.common.collect.ForwardingObject;

class NewMailQueueImpl implements NewMailQueue {

	private static abstract class ForwardingQueueableNewMail extends ForwardingObject implements QueueableNewMail {

		/**
		 * Usable by subclasses only.
		 */
		protected ForwardingQueueableNewMail() {
		}

		@Override
		protected abstract QueueableNewMail delegate();

		@Override
		public QueueableNewMail withTo(final Iterable<String> tos) {
			return delegate().withTo(tos);
		}

		@Override
		public QueueableNewMail withTo(final String... tos) {
			return delegate().withTo(tos);
		}

		@Override
		public QueueableNewMail withTo(final String to) {
			return delegate().withTo(to);
		}

		@Override
		public QueueableNewMail withSubject(final String subject) {
			return delegate().withSubject(subject);
		}

		@Override
		public QueueableNewMail withFrom(final String from) {
			return delegate().withFrom(from);
		}

		@Override
		public QueueableNewMail withContentType(final String contentType) {
			return delegate().withContentType(contentType);
		}

		@Override
		public QueueableNewMail withContent(final String content) {
			return delegate().withContent(content);
		}

		@Override
		public QueueableNewMail withCc(final Iterable<String> ccs) {
			return delegate().withCc(ccs);
		}

		@Override
		public QueueableNewMail withCc(final String... ccs) {
			return delegate().withCc(ccs);
		}

		@Override
		public QueueableNewMail withCc(final String cc) {
			return delegate().withCc(cc);
		}

		@Override
		public QueueableNewMail withBcc(final Iterable<String> bccs) {
			return delegate().withBcc(bccs);
		}

		@Override
		public QueueableNewMail withBcc(final String... bccs) {
			return delegate().withBcc(bccs);
		}

		@Override
		public QueueableNewMail withBcc(final String bcc) {
			return delegate().withBcc(bcc);
		}

		@Override
		public QueueableNewMail withAttachment(final DataHandler dataHandler, final String name) {
			return delegate().withAttachment(dataHandler, name);
		}

		@Override
		public QueueableNewMail withAttachment(final DataHandler dataHandler) {
			return delegate().withAttachment(dataHandler);
		}

		@Override
		public QueueableNewMail withAttachment(final String url, final String name) {
			return delegate().withAttachment(url, name);
		}

		@Override
		public QueueableNewMail withAttachment(final String url) {
			return delegate().withAttachment(url);
		}

		@Override
		public QueueableNewMail withAttachment(final URL url, final String name) {
			return delegate().withAttachment(url, name);
		}

		@Override
		public QueueableNewMail withAttachment(final URL url) {
			return delegate().withAttachment(url);
		}

		@Override
		public NewMailQueue add() {
			return delegate().add();
		}

	}

	private static class QueueableNewMailImpl extends ForwardingNewMail implements QueueableNewMail {

		private final NewMailQueue parent;
		private final Callback callback;
		private final NewMailImpl newMail;
		private final Collection<? super NewMailImpl> elements;

		public QueueableNewMailImpl(final NewMailQueue parent, final Callback callback, final NewMailImpl newMail,
				final Collection<? super NewMailImpl> elements) {
			this.parent = parent;
			this.callback = callback;
			this.newMail = newMail;
			this.elements = elements;
		}

		@Override
		protected NewMail delegate() {
			return newMail;
		}

		@Override
		public QueueableNewMail withFrom(final String from) {
			delegate().withFrom(from);
			return this;
		}

		@Override
		public QueueableNewMail withTo(final String to) {
			delegate().withTo(to);
			return this;
		}

		@Override
		public QueueableNewMail withTo(final String... tos) {
			delegate().withTo(tos);
			return this;
		}

		@Override
		public QueueableNewMail withTo(final Iterable<String> tos) {
			delegate().withTo(tos);
			return this;
		}

		@Override
		public QueueableNewMail withCc(final String cc) {
			delegate().withCc(cc);
			return this;
		}

		@Override
		public QueueableNewMail withCc(final String... ccs) {
			delegate().withCc(ccs);
			return this;
		}

		@Override
		public QueueableNewMail withCc(final Iterable<String> ccs) {
			delegate().withCc(ccs);
			return this;
		}

		@Override
		public QueueableNewMail withBcc(final String bcc) {
			delegate().withBcc(bcc);
			return this;
		}

		@Override
		public QueueableNewMail withBcc(final String... bccs) {
			delegate().withBcc(bccs);
			return this;
		}

		@Override
		public QueueableNewMail withBcc(final Iterable<String> bccs) {
			delegate().withBcc(bccs);
			return this;
		}

		@Override
		public QueueableNewMail withSubject(final String subject) {
			delegate().withSubject(subject);
			return this;
		}

		@Override
		public QueueableNewMail withContent(final String body) {
			delegate().withContent(body);
			return this;
		}

		@Override
		public QueueableNewMail withContentType(final String contentType) {
			delegate().withContentType(contentType);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final URL url) {
			delegate().withAttachment(url);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final URL url, final String name) {
			delegate().withAttachment(url, name);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final String url) {
			delegate().withAttachment(url);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final String url, final String name) {
			delegate().withAttachment(url, name);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final DataHandler dataHandler) {
			delegate().withAttachment(dataHandler);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final DataHandler dataHandler, final String name) {
			delegate().withAttachment(dataHandler, name);
			return this;
		}

		@Override
		public NewMailQueue add() {
			elements.add(newMail);
			callback.added(elements.size() - 1);
			return parent;
		}

	}

	private static final Callback NULL_CALLBACK = new Callback() {

		@Override
		public void added(final int index) {
			// nothing to do
		}

		@Override
		public void sent(final int index) {
			// nothing to do
		}

	};

	private final Output configuration;
	private final Logger logger;
	private final Collection<NewMailImpl> elements;
	private Callback callback = NULL_CALLBACK;

	public NewMailQueueImpl(final Output configuration) {
		this.configuration = configuration;
		this.logger = configuration.getLogger();
		// we need to preserve order
		this.elements = newArrayList();
	}

	@Override
	public NewMailQueue withCallback(final Callback callback) {
		this.callback = defaultIfNull(callback, NULL_CALLBACK);
		return this;
	}

	@Override
	public QueueableNewMail newMail() {
		final NewMailImpl newMail = new NewMailImpl(logger);
		return new QueueableNewMailImpl(this, callback, newMail, elements);
	}

	@Override
	public void sendAll() {
		new OutputTemplate(configuration).execute(new Hook() {

			@Override
			public void connected(final Session session, final Transport transport) throws MailException {
				try {
					int count = 0;
					for (final NewMailImpl element : elements) {
						final MessageBuilder messageBuilder = new NewMailImplMessageBuilder(configuration, session,
								element);
						final Message message = messageBuilder.build();
						transport.sendMessage(message, message.getAllRecipients());
						callback.sent(count++);
					}
				} catch (final MessagingException e) {
					logger.error("error sending mail", e);
					throw MailException.send(e);
				}
			}

		});
	}

}
