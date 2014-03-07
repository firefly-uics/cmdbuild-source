package unit.services.email;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.services.email.EmailPersistence;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.SubjectParser;
import org.junit.Before;
import org.junit.Test;

public class EmailServiceTest {

	private EmailService emailService;

	private EmailConfiguration configuration;
	private MailApiFactory factory;
	private EmailPersistence persistence;
	private SubjectParser subjectParser;

	@Before
	public void setUp() throws Exception {
		configuration = mock(EmailConfiguration.class);
		factory = mock(MailApiFactory.class);
		persistence = mock(EmailPersistence.class);
		subjectParser = mock(SubjectParser.class);

		emailService = new EmailService(configuration, factory, persistence, subjectParser);
	}

	@Test
	public void resolveRecipientsAsSpecificUser() {
		// given
		when(persistence.getEmailsForUser("foo")) //
				.thenReturn(Collections.<String> emptyList());

		// when
		emailService.resolveRecipients(Arrays.asList("[user]foo"));

		// then
		verify(persistence).getEmailsForUser("foo");
	}

	@Test
	public void resolveRecipientsAsSpecificGroup() {
		// given
		when(persistence.getEmailsForGroup("foo")) //
				.thenReturn(Collections.<String> emptyList());

		// when
		emailService.resolveRecipients(Arrays.asList("[group]foo"));

		// then
		verify(persistence).getEmailsForGroup("foo");
	}

	@Test
	public void resolveRecipientsAsSpecificGroupUsers() {
		// given
		when(persistence.getEmailsForGroupUsers("foo")) //
				.thenReturn(Collections.<String> emptyList());

		// when
		emailService.resolveRecipients(Arrays.asList("[groupUsers]foo"));

		// then
		verify(persistence).getEmailsForGroupUsers("foo");
	}

}
