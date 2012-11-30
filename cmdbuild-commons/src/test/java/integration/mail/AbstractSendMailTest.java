package integration.mail;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.mail.internet.MimeMessage;

import org.cmdbuild.common.mail.DefaultMailApiFactory;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.MailApi.Configuration;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.common.mail.NewMail;
import org.junit.After;
import org.junit.Before;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public abstract class AbstractSendMailTest {

	private static final String LOCALHOST = "localhost";

	protected static final String FOO_USER = "foo";
	protected static final String BAR_USER = "bar";
	protected static final String BAZ_USER = "baz";

	protected static final String PASSWORD = "s3cret";

	private static final String AT = "@";
	private static final String DOMAIN_EXAMPLE_DOT_COM = "example.com";

	protected static final String FOO_AT_EXAMPLE_DOT_COM = FOO_USER + AT + DOMAIN_EXAMPLE_DOT_COM;
	protected static final String BAR_AT_EXAMPLE_DOT_COM = BAR_USER + AT + DOMAIN_EXAMPLE_DOT_COM;
	protected static final String BAZ_AT_EXAMPLE_DOT_COM = BAZ_USER + AT + DOMAIN_EXAMPLE_DOT_COM;

	protected static final String SUBJECT = "this is the subject";
	protected static final String PLAIN_TEXT_CONTENT = "this is the body";

	protected static final String MIME_TEXT_PLAIN = "text/plain";
	protected static final String MIME_TEXT_HTML = "text/html";

	private static final String NO_USERNAME = null;
	private static final String NO_PASSWORD = null;

	protected GreenMail greenmail;

	@Before
	public void startMailServer() throws Exception {
		beforeServerStart();
		greenmail = new GreenMail(serverSetup());
		greenmail.start();
	}

	protected void beforeServerStart() {
		// default nothing to do
	}

	protected abstract ServerSetup serverSetup();

	@After
	public void stopMailServer() throws Exception {
		greenmail.stop();
	}

	/*
	 * Utils
	 */

	protected NewMail newMail() {
		return newMail(NO_USERNAME, NO_PASSWORD);
	}

	protected NewMail newMail(final String username, final String password) {
		final MailApiFactory mailApiFactory = new DefaultMailApiFactory();
		mailApiFactory.setConfiguration(configurationFrom(username, password));
		final MailApi mailApi = mailApiFactory.createMailApi();
		return mailApi.newMail();
	}

	protected void send(final NewMail newMail) throws Exception {
		newMail.send();
		assertThat(greenmail.waitForIncomingEmail(1), equalTo(true));
		assertThat(greenmail.getReceivedMessages().length, equalTo(1));
	}

	protected Configuration configurationFrom(final String username, final String password) {
		return new MailApi.Configuration() {

			@Override
			public boolean isDebug() {
				return true;
			}

			@Override
			public String getOutputProtocol() {
				return serverSetup().getProtocol();
			}

			@Override
			public String getOutputHost() {
				return LOCALHOST;
			}

			@Override
			public Integer getOutputPort() {
				return serverSetup().getPort();
			}

			@Override
			public boolean isStartTlsEnabled() {
				return false;
			}

			@Override
			public String getOutputUsername() {
				return username;
			}

			@Override
			public String getOutputPassword() {
				return password;
			}

			@Override
			public List<String> getOutputFromRecipients() {
				return asList(FOO_AT_EXAMPLE_DOT_COM);
			}

		};
	}

	protected MimeMessage firstReceivedMessage() {
		return greenmail.getReceivedMessages()[0];
	}

}
