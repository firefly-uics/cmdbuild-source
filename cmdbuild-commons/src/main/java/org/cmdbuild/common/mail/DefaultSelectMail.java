package org.cmdbuild.common.mail;

import static org.cmdbuild.common.mail.JavaxMailUtils.messageIdOf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Store;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.common.mail.GetMail.Attachment;
import org.cmdbuild.common.mail.InputTemplate.Hooks;
import org.cmdbuild.common.mail.MailApi.InputConfiguration;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

class DefaultSelectMail implements SelectMail {

	private class DefaultAttachment implements Attachment {

		private final URL url;

		public DefaultAttachment(final File file) throws MalformedURLException {
			this.url = file.toURI().toURL();
		}

		@Override
		public URL getUrl() {
			return url;
		}

	}

	private class ContentExtractor {

		private static final String ATTACHMENT_PREFIX = "attachment";
		private static final String ATTACHMENT_EXTENSION = ".out";

		private final Logger logger = DefaultSelectMail.this.logger;

		private String content;
		private final List<Attachment> attachments = Lists.newArrayList();

		public ContentExtractor(final Message message) throws MailException {
			try {
				handle(message);
			} catch (final MessagingException e) {
				throw MailException.content(e);
			} catch (final IOException e) {
				throw MailException.io(e);
			}
		}

		private void handle(final Message message) throws MessagingException, IOException {
			final Object content = message.getContent();
			if (content instanceof Multipart) {
				handleMultipart(Multipart.class.cast(content));
			} else {
				handlePart(message);
			}
		}

		private void handleMultipart(final Multipart multipart) throws MessagingException, IOException {
			for (int i = 0, n = multipart.getCount(); i < n; i++) {
				handlePart(multipart.getBodyPart(i));
			}
		}

		private void handlePart(final Part part) throws MessagingException, IOException {
			final String contentType = part.getContentType();
			logger.debug("content-type for current part is '{}'", contentType);
			final String disposition = part.getDisposition();
			if (disposition == null) {
				content = part.getContent().toString();
			} else if (Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
				logger.debug("attachment with name '{}'", part.getFileName());
				handleAttachment(part);
			} else if (Part.INLINE.equalsIgnoreCase(disposition)) {
				logger.debug("in-line with name '{}'", part.getFileName());
				handleAttachment(part);
			} else {
				logger.warn("should never happen, disposition is '{}'", disposition);
			}
		}

		private void handleAttachment(final Part part) throws MessagingException, IOException {
			final File directory = FileUtils.getTempDirectory();
			final String filename;
			if (part.getFileName() == null) {
				filename = File.createTempFile(ATTACHMENT_PREFIX, ATTACHMENT_EXTENSION, directory).getName();
			} else {
				filename = part.getFileName();
			}
			logger.trace("saving file '{}'", filename);

			File file = new File(directory, filename);
			for (int i = 0; file.exists(); i++) {
				file = new File(directory, filename + i);
			}

			final InputStream is = part.getInputStream();
			FileUtils.copyInputStreamToFile(is, file);

			attachments.add(new DefaultAttachment(file));
		}

		public String getContent() {
			return content;
		}

		public Iterable<Attachment> getAttachments() {
			return attachments;
		}

	}

	private final InputConfiguration configuration;
	private final Logger logger;
	private final FetchedMail mail;

	private GetMail getMail;
	private String targetFolder;

	public DefaultSelectMail(final InputConfiguration configuration, final FetchedMail fetchedMail) {
		this.configuration = configuration;
		this.logger = configuration.getLogger();
		this.mail = fetchedMail;
	}

	@Override
	public GetMail get() throws MailException {
		logger.info("getting specified mail");
		final InputTemplate inputTemplate = new InputTemplate(configuration);
		inputTemplate.execute(new Hooks() {

			@Override
			public void connected(final Store store) {
				try {
					final Folder inbox = store.getFolder(mail.getFolder());
					inbox.open(Folder.READ_ONLY);

					final List<Message> messages = Arrays.asList(inbox.getMessages());
					for (final Message message : messages) {
						if (mail.getId().equals(messageIdOf(message))) {
							getMail = transform(message);
							break;
						}
					}
				} catch (final MessagingException e) {
					logger.error("error getting mail", e);
					throw MailException.get(e);
				} catch (final IOException e) {
					logger.error("error getting mail", e);
					throw MailException.io(e);
				}
			}

		});
		return getMail;
	}

	private GetMail transform(final Message message) throws MessagingException, IOException {
		final ContentExtractor contentExtractor = new ContentExtractor(message);
		return DefaultGetMail.newInstance() //
				.withId(messageIdOf(message)) //
				.withFolder(message.getFolder().getFullName()) //
				.withSubject(message.getSubject()) //
				.withContent(contentExtractor.getContent()) //
				.withAttachments(contentExtractor.getAttachments()) //
				.build();
	}

	@Override
	public SelectMail selectTargetFolder(final String folder) {
		logger.info("selects folder '{}'", folder);
		this.targetFolder = folder;
		return this;
	}

	@Override
	public void move() throws MailException {
		logger.info("moving mail with id '{}' to folder '{}'", mail.getId(), targetFolder);
		final InputTemplate inputTemplate = new InputTemplate(configuration);
		inputTemplate.execute(new Hooks() {

			@Override
			public void connected(final Store store) {
				try {
					final Folder inbox = store.getFolder(mail.getFolder());
					inbox.open(Folder.READ_WRITE);

					final List<Message> messages = Arrays.asList(inbox.getMessages());
					for (final Message message : messages) {
						if (mail.getId().equals(messageIdOf(message))) {
							final Message[] singleMessageArray = new Message[] { message };
							inbox.copyMessages(singleMessageArray, getOrCreate(store, targetFolder));
							inbox.setFlags(singleMessageArray, new Flags(Flags.Flag.DELETED), true);
							inbox.expunge();
							break;
						}
					}
				} catch (final MessagingException e) {
					logger.error("error getting mail", e);
					throw MailException.move(e);
				}
			}

			private Folder getOrCreate(final Store store, final String name) throws MessagingException {
				final Folder folder = store.getFolder(name);
				if (!folder.exists()) {
					folder.create(Folder.HOLDS_MESSAGES);
				}
				return folder;
			}

		});
	}

}
