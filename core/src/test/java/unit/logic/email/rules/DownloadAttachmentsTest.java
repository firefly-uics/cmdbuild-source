package unit.logic.email.rules;

import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_CLASS_NAME;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.cmdbuild.logic.email.rules.AttachmentStore;
import org.cmdbuild.logic.email.rules.AttachmentStoreFactory;
import org.cmdbuild.logic.email.rules.DownloadAttachments;
import org.cmdbuild.model.email.Attachment;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.services.email.EmailCallbackHandler.RuleAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloadAttachmentsTest {

	private static final Long ID = 42L;

	private DownloadAttachments downloadAttachments;

	private AttachmentStore attachmentStore;
	private AttachmentStoreFactory attachmentStoreFactory;

	@Captor
	private ArgumentCaptor<Iterable<Attachment>> attachmentsCaptor;

	@Before
	public void setUp() throws Exception {
		attachmentStore = mock(AttachmentStore.class);

		attachmentStoreFactory = mock(AttachmentStoreFactory.class);
		when(attachmentStoreFactory.create(EMAIL_CLASS_NAME, ID)) //
				.thenReturn(attachmentStore);

		downloadAttachments = new DownloadAttachments(attachmentStoreFactory);
	}

	@Test
	public void alwaysApplies() throws Exception {
		assertThat(downloadAttachments.applies(null), is(true));
		assertThat(downloadAttachments.applies(new Email()), is(true));
	}

	@Test
	public void emailNotAdapted() throws Exception {
		// given
		final Email email = new Email();

		// when
		final Email adaptedEmail = downloadAttachments.adapt(email);

		// then
		assertThat(adaptedEmail, equalTo(email));
	}

	@Test
	public void attachmentStoreCalled() throws Exception {
		// given
		final Attachment attachment = Attachment.newInstance().build();
		final Email email = new Email(ID);
		email.setAttachments(Arrays.asList(attachment));
		final RuleAction ruleAction = downloadAttachments.action(email);

		// when
		ruleAction.execute();

		// then
		verify(attachmentStoreFactory).create(EMAIL_CLASS_NAME, ID);
		verify(attachmentStore).store(attachmentsCaptor.capture());
		assertThat(attachmentsCaptor.getValue(), containsInAnyOrder(attachment));
	}

}
