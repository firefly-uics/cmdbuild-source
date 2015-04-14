package org.cmdbuild.common.api.mail.javax.mail;

import java.net.URL;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.cmdbuild.common.api.mail.Configuration.Output;
import org.cmdbuild.common.api.mail.ForwardingNewMail;
import org.cmdbuild.common.api.mail.MailException;
import org.cmdbuild.common.api.mail.NewMail;
import org.cmdbuild.common.api.mail.SendableNewMail;
import org.cmdbuild.common.api.mail.javax.mail.OutputTemplate.Hook;
import org.slf4j.Logger;

class SendableNewMailImpl extends ForwardingNewMail implements SendableNewMail {

	private final Output configuration;
	private final Logger logger;

	private final NewMailImpl newMail;
	private boolean asynchronous;

	public SendableNewMailImpl(final Output configuration) {
		this.configuration = configuration;
		this.logger = configuration.getLogger();
		newMail = new NewMailImpl(logger);
	}

	@Override
	protected NewMail delegate() {
		return newMail;
	}

	@Override
	public SendableNewMail withFrom(final String from) {
		delegate().withFrom(from);
		return this;
	}

	@Override
	public SendableNewMail withTo(final String to) {
		delegate().withTo(to);
		return this;
	}

	@Override
	public SendableNewMail withTo(final String... tos) {
		delegate().withTo(tos);
		return this;
	}

	@Override
	public SendableNewMail withTo(final Iterable<String> tos) {
		delegate().withTo(tos);
		return this;
	}

	@Override
	public SendableNewMail withCc(final String cc) {
		delegate().withCc(cc);
		return this;
	}

	@Override
	public SendableNewMail withCc(final String... ccs) {
		delegate().withCc(ccs);
		return this;
	}

	@Override
	public SendableNewMail withCc(final Iterable<String> ccs) {
		delegate().withCc(ccs);
		return this;
	}

	@Override
	public SendableNewMail withBcc(final String bcc) {
		delegate().withBcc(bcc);
		return this;
	}

	@Override
	public SendableNewMail withBcc(final String... bccs) {
		delegate().withBcc(bccs);
		return this;
	}

	@Override
	public SendableNewMail withBcc(final Iterable<String> bccs) {
		delegate().withBcc(bccs);
		return this;
	}

	@Override
	public SendableNewMail withSubject(final String subject) {
		delegate().withSubject(subject);
		return this;
	}

	@Override
	public SendableNewMail withContent(final String body) {
		delegate().withContent(body);
		return this;
	}

	@Override
	public SendableNewMail withContentType(final String contentType) {
		delegate().withContentType(contentType);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final URL url) {
		delegate().withAttachment(url);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final URL url, final String name) {
		delegate().withAttachment(url, name);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final String url) {
		delegate().withAttachment(url);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final String url, final String name) {
		delegate().withAttachment(url, name);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final DataHandler dataHandler) {
		delegate().withAttachment(dataHandler);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final DataHandler dataHandler, final String name) {
		delegate().withAttachment(dataHandler, name);
		return this;
	}

	@Override
	public SendableNewMail withAsynchronousSend(final boolean asynchronous) {
		this.asynchronous = asynchronous;
		return this;
	}

	@Override
	public void send() {
		final Runnable job = sendJob();
		if (asynchronous) {
			runInAnotherThread(job);
		} else {
			job.run();
		}
	}

	private Runnable sendJob() {
		return new Runnable() {

			@Override
			public void run() {
				try {
					new OutputTemplate(configuration).execute(new Hook() {

						@Override
						public void connected(final Session session, final Transport transport) throws MailException {
							try {
								final MessageBuilder messageBuilder = new NewMailImplMessageBuilder(configuration,
										session, newMail);
								final Message message = messageBuilder.build();
								transport.sendMessage(message, message.getAllRecipients());
							} catch (final MessagingException e) {
								logger.error("error sending mail", e);
								throw MailException.send(e);
							}
						}

					});

				} catch (final RuntimeException e) {
					logger.error("error sending e-mail", e);
					throw e;
				}
			}

		};
	}

	private void runInAnotherThread(final Runnable job) {
		new Thread(job).start();
	}

}
