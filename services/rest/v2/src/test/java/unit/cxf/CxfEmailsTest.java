package unit.cxf;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.draft;
import static org.cmdbuild.service.rest.v2.model.Models.newEmail;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.cmdbuild.logic.email.EmailImpl;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.service.rest.v2.cxf.CxfEmails;
import org.cmdbuild.service.rest.v2.model.Email;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.Test;

public class CxfEmailsTest {

	private EmailLogic emailLogic;

	private CxfEmails underTest;

	@Before
	public void setUp() throws Exception {
		emailLogic = mock(EmailLogic.class);
		underTest = new CxfEmails(emailLogic);
	}

	@Test(expected = RuntimeException.class)
	public void createFailsWithExceptionWhenLogicThrowsException() throws Exception {
		// given
		final Throwable e = new RuntimeException();
		doThrow(e) //
				.when(emailLogic).create(eq(EmailImpl.newInstance() //
						.withFromAddress("from@example.com") //
						.withToAddresses("to@example.com") //
						.withSubject("subject") //
						.withContent("body") //
						.build()));

		// when
		underTest.create(newEmail() //
				.withFrom("from@example.com") //
				.withTo(asList("to@example.com")) //
				.withSubject("subject") //
				.withBody("body") //
				.build());
	}

	@Test
	public void createReturnsIdReturnedFromLogic() throws Exception {
		// given
		doReturn(42L) //
				.when(emailLogic).create(any(EmailLogic.Email.class));

		// when
		final ResponseSingle<Long> response = underTest.create(newEmail() //
				.withFrom("from@example.com") //
				.withTo(asList("to@example.com")) //
				.withCc(asList("cc@example.com", "another_cc@gmail.com")) //
				.withBcc(asList("bcc@example.com")) //
				.withSubject("subject") //
				.withBody("body") //
				.withStatus("draft") //
				.withReference(34L) //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemporary(true) //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build());

		// then
		assertThat(response.getElement(), equalTo(42L));

		verify(emailLogic).create(eq(EmailImpl.newInstance() //
				.withFromAddress("from@example.com") //
				.withToAddresses("to@example.com") //
				.withCcAddresses("cc@example.com,another_cc@gmail.com") //
				.withBccAddresses("bcc@example.com") //
				.withSubject("subject") //
				.withContent("body") //
				.withStatus(draft()) //
				.withActivityId(34L) //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemporary(true) //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build()));
	}

	@Test(expected = RuntimeException.class)
	public void readAllFailsWithExceptionWhenLogicThrowsException() throws Exception {
		// given
		final Throwable e = new RuntimeException();
		doThrow(e) //
				.when(emailLogic).readAll(isNull(Long.class));

		// when
		underTest.readAll(null, null, null);
	}

	@Test
	public void readAllPassesNullValueToLogicWhenFilterIsMissing() throws Exception {
		// given
		doReturn(EMPTY_LIST) //
				.when(emailLogic).readAll(any(Long.class));

		// when
		underTest.readAll(null, 10, 0);

		// then
		verify(emailLogic).readAll(null);
	}

	@Test
	public void readAllCanLimitOutput() throws Exception {
		// given
		final EmailLogic.Email email_1 = EmailImpl.newInstance() //
				.withId(1L) //
				.build();
		final EmailLogic.Email email_2 = EmailImpl.newInstance() //
				.withId(2L) //
				.build();
		final EmailLogic.Email email_3 = EmailImpl.newInstance() //
				.withId(3L) //
				.build();
		final EmailLogic.Email email_4 = EmailImpl.newInstance() //
				.withId(3L) //
				.build();
		doReturn(asList(email_1, email_2, email_3, email_4)) //
				.when(emailLogic).readAll(any(Long.class));

		// when
		final ResponseMultiple<Long> response = underTest.readAll(null, 1, 2);

		// then
		assertThat(newArrayList(response.getElements()), equalTo(asList(email_3.getId())));
		assertThat(response.getMetadata().getTotal(), equalTo(4L));

		verify(emailLogic).readAll(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void readAllDoesNotSupportFilterWithLogicalOperator() throws Exception {
		// when
		underTest.readAll("{" + //
				"\"attribute\": {" + //
				"    \"and\": [{" + //
				"        \"simple\": {" + //
				"            \"attribute\": \"reference\"," + //
				"            \"operator\": \"equal\"," + //
				"            \"value\": [1]" + //
				"        }" + //
				"        }, {" + //
				"        \"simple\": {" + //
				"            \"attribute\": \"reference\"," + //
				"            \"operator\": \"equal\"," + //
				"            \"value\": [2]" + //
				"        }" + //
				"    }]" + //
				"}" + //
				"}", null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void readAllDoesNotSupportFilterWithOperatorsDifferentFromEqual() throws Exception {
		// when
		underTest.readAll("{" + //
				"\"attribute\": {" + //
				"    \"simple\": {" + //
				"        \"attribute\": \"reference\"," + //
				"        \"operator\": \"notequal\"," + //
				"        \"value\": [1]" + //
				"    }" + //
				"}" + //
				"}", null, null);
	}

	@Test
	public void readAllSupportsFilterWithEqualOperatorOnly() throws Exception {
		// given
		doReturn(EMPTY_LIST) //
				.when(emailLogic).readAll(any(Long.class));

		// when
		underTest.readAll("{" + //
				"\"attribute\": {" + //
				"    \"simple\": {" + //
				"        \"attribute\": \"reference\"," + //
				"        \"operator\": \"equal\"," + //
				"        \"value\": [42]" + //
				"    }" + //
				"}" + //
				"}", null, null);

		// then
		verify(emailLogic).readAll(42L);
	}

	@Test(expected = RuntimeException.class)
	public void readFailsWithExceptionWhenLogicThrowsException() throws Exception {
		// given
		final Throwable e = new RuntimeException();
		doThrow(e) //
				.when(emailLogic).read(eq(EmailImpl.newInstance() //
						.withId(42L).build()));

		// when
		underTest.read(42L);
	}

	@Test
	public void readReturnsIdReturnedFromLogic() throws Exception {
		// given
		final EmailLogic.Email read = EmailImpl.newInstance() //
				.withId(12L) //
				.withFromAddress("from@example.com") //
				.withToAddresses("to@example.com") //
				.withCcAddresses("cc@example.com,another_cc@gmail.com") //
				.withBccAddresses("bcc@example.com") //
				.withSubject("subject") //
				.withContent("body") //
				.withStatus(draft()) //
				.withActivityId(34L) //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemporary(true) //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build();
		doReturn(read) //
				.when(emailLogic).read(any(EmailLogic.Email.class));

		// when
		final ResponseSingle<Email> response = underTest.read(42L);

		// then
		assertThat(response.getElement(), equalTo(newEmail() //
				.withId(12L) //
				.withFrom("from@example.com") //
				.withTo(asList("to@example.com")) //
				.withCc(asList("cc@example.com", "another_cc@gmail.com")) //
				.withBcc(asList("bcc@example.com")) //
				.withSubject("subject") //
				.withBody("body") //
				.withStatus("draft") //
				.withReference(34L) //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemporary(true) //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build()));

		verify(emailLogic).read(eq(EmailImpl.newInstance() //
				.withId(42L) //
				.build()));
	}

	@Test(expected = RuntimeException.class)
	public void updateFailsWithExceptionWhenLogicThrowsException() throws Exception {
		// given
		final Throwable e = new RuntimeException();
		doThrow(e) //
				.when(emailLogic).update(eq(EmailImpl.newInstance() //
						.withId(42L) //
						.withFromAddress("from@example.com") //
						.withToAddresses("to@example.com") //
						.withSubject("subject") //
						.withContent("body") //
						.build()));

		// when
		underTest.update(42L, newEmail() //
				.withId(12L) //
				.withFrom("from@example.com") //
				.withTo(asList("to@example.com")) //
				.withSubject("subject") //
				.withBody("body") //
				.build());
	}

	@Test
	public void updateReturnsIdReturnedFromLogic() throws Exception {
		// when
		underTest.update(42L, newEmail() //
				.withId(12L) //
				.withFrom("from@example.com") //
				.withTo(asList("to@example.com")) //
				.withCc(asList("cc@example.com", "another_cc@gmail.com")) //
				.withBcc(asList("bcc@example.com")) //
				.withSubject("subject") //
				.withBody("body") //
				.withStatus("draft") //
				.withReference(34L) //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemporary(true) //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build());

		// then
		verify(emailLogic).update(eq(EmailImpl.newInstance() //
				.withId(42L) //
				.withFromAddress("from@example.com") //
				.withToAddresses("to@example.com") //
				.withCcAddresses("cc@example.com,another_cc@gmail.com") //
				.withBccAddresses("bcc@example.com") //
				.withSubject("subject") //
				.withContent("body") //
				.withStatus(draft()) //
				.withActivityId(34L) //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemporary(true) //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build()));
	}

	@Test(expected = RuntimeException.class)
	public void deleteFailsWithExceptionWhenLogicThrowsException() throws Exception {
		// given
		final Throwable e = new RuntimeException();
		doThrow(e) //
				.when(emailLogic).delete(eq(EmailImpl.newInstance() //
						.withId(42L) //
						.build()));

		// when
		underTest.delete(42L);
	}

	@Test
	public void deleteReturnsIdReturnedFromLogic() throws Exception {
		// when
		underTest.delete(42L);

		// then
		verify(emailLogic).delete(eq(EmailImpl.newInstance() //
				.withId(42L) //
				.build()));
	}

}
