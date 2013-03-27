package integration.mail;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.security.Security;

import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;

public class SendMailThroughSslTest extends AbstractSendMailTest {

	@Override
	protected void beforeServerStart() {
		final String SSL_SOCKET_FACTORY_PROVIDER = "ssl.SocketFactory.provider";
		Security.setProperty(SSL_SOCKET_FACTORY_PROVIDER, DummySSLSocketFactory.class.getName());
	}

	@Override
	protected ServerSetup serverSetup() {
		return ServerSetupTest.SMTPS;
	}

	@Test
	public void mailSendAndReceived() throws Exception {
		send(newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		assertThat(greenmail.getReceivedMessages().length, equalTo(1));

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO)[0].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getSubject(), equalTo(SUBJECT));
		assertThat(getBody(receivedMessage), equalTo(PLAIN_TEXT_CONTENT));
	}

}