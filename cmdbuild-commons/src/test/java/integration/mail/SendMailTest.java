package integration.mail;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.UUID;

import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;

public class SendMailTest extends AbstractSendMailTest {

	private static final String ATTACHMENT_FILE_PREFIX = "attachment";

	protected static final String ATTACHMENT_CONTENT = UUID.randomUUID().toString();
	protected static final int ATTACHMENT_BODY_PART = 1;

	@Override
	protected ServerSetup serverSetup() {
		return ServerSetupTest.SMTP;
	}

	@Test
	public void ifNotSpecifiedFromsAreTheValuesSpecifiedWithinTheConfiguration() throws Exception {
		send(newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getFrom().length, equalTo(1));
		assertThat(receivedMessage.getFrom()[0].toString(), equalTo(FOO_AT_EXAMPLE_DOT_COM));
	}

	@Test
	public void ifFromIsSpecifiedFromIsNotReadedFromConfiguration() throws Exception {
		send(newMail() //
				.withFrom(BAR_AT_EXAMPLE_DOT_COM) //
				.withFrom(BAZ_AT_EXAMPLE_DOT_COM) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getFrom().length, equalTo(2));
		assertThat(receivedMessage.getFrom()[0].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getFrom()[1].toString(), equalTo(BAZ_AT_EXAMPLE_DOT_COM));
	}

	@Test
	public void multipleFromsWithTheSameValueAreOk() throws Exception {
		send(newMail() //
				.withFrom(BAZ_AT_EXAMPLE_DOT_COM) //
				.withFrom(BAR_AT_EXAMPLE_DOT_COM) //
				.withFrom(BAZ_AT_EXAMPLE_DOT_COM) //
				.withFrom(BAR_AT_EXAMPLE_DOT_COM) //
				.withFrom(BAZ_AT_EXAMPLE_DOT_COM) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getFrom().length, equalTo(5));
		assertThat(receivedMessage.getFrom()[0].toString(), equalTo(BAZ_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getFrom()[1].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getFrom()[2].toString(), equalTo(BAZ_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getFrom()[3].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getFrom()[4].toString(), equalTo(BAZ_AT_EXAMPLE_DOT_COM));
	}

	@Test
	public void onlyToRecipientsOthersAreNull() throws Exception {
		send(newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO), not(is(nullValue())));
		assertThat(receivedMessage.getRecipients(RecipientType.CC), is(nullValue()));
		assertThat(receivedMessage.getRecipients(RecipientType.BCC), is(nullValue()));
	}

	@Test
	public void onlyCcRecipientsOthersAreNull() throws Exception {
		send(newMail() //
				.withCc(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO), is(nullValue()));
		assertThat(receivedMessage.getRecipients(RecipientType.CC), not(is(nullValue())));
		assertThat(receivedMessage.getRecipients(RecipientType.BCC), is(nullValue()));
	}

	@Test
	public void onlyBccRecipientsOthersAreNull() throws Exception {
		send(newMail() //
				.withBcc(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO), is(nullValue()));
		assertThat(receivedMessage.getRecipients(RecipientType.CC), is(nullValue()));
		assertThat(receivedMessage.getRecipients(RecipientType.BCC), is(nullValue()));
	}

	@Test
	public void plainTextMessageSuccessfullySent() throws Exception {
		send(newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO)[0].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getSubject(), equalTo(SUBJECT));
		assertThat(getBody(receivedMessage), equalTo(PLAIN_TEXT_CONTENT));
	}

	@Test
	public void plainTextMessageSuccessfullySentWithAutentication() throws Exception {
		greenmail.setUser(FOO_AT_EXAMPLE_DOT_COM, FOO_USER, PASSWORD);

		send(newMail(FOO_USER, PASSWORD) //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO)[0].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getSubject(), equalTo(SUBJECT));
		assertThat(getBody(receivedMessage), equalTo(PLAIN_TEXT_CONTENT));
	}

	@Test
	public void attachmentSuccessfullySent() throws Exception {
		final URL attachment = newAttachmentFileFromContent(ATTACHMENT_CONTENT);

		send(newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.withAttachment(attachment));

		assertThat(receivedAttachmentContent(), equalTo(ATTACHMENT_CONTENT));
	}

	@Test
	public void defaultMimeTypeIsTextPlain() throws Exception {
		send(newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		assertThat(firstReceivedMessage().getContentType(), startsWith(MIME_TEXT_PLAIN));
	}

	@Test
	public void customMimeTypeSuccessfullySetted() throws Exception {
		send(newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.withContentType(MIME_TEXT_HTML));

		assertThat(((MimeMultipart) firstReceivedMessage().getContent()).getBodyPart(0).getContentType(),
				startsWith(MIME_TEXT_HTML));
	}

	private URL newAttachmentFileFromContent(final String content) throws IOException {
		final File file = File.createTempFile(ATTACHMENT_FILE_PREFIX, null);
		FileUtils.writeStringToFile(file, content);
		return file.toURI().toURL();
	}

	private String receivedAttachmentContent() throws IOException, MessagingException {
		final MimeMultipart mimeMultipart = MimeMultipart.class.cast(firstReceivedMessage().getContent());
		final BodyPart bodyPart = mimeMultipart.getBodyPart(ATTACHMENT_BODY_PART);
		final InputStream stream = bodyPart.getInputStream();
		final StringWriter writer = new StringWriter();
		IOUtils.copy(stream, writer);
		final String receivedAttachmentContent = writer.toString();
		return receivedAttachmentContent;
	}

}