package unit.api.fluent.ws;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.cmdbuild.api.fluent.Attachment;
import org.cmdbuild.api.fluent.AttachmentDescriptor;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.SelectedAttachments;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

public class SelectedAttachmentsTest extends AbstractWsFluentApiTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private SelectedAttachments selectedAttachments;

	@Test
	public void allAttachmentsSelected() throws Exception {
		// given
		final org.cmdbuild.services.soap.Attachment foo = soapAttachment("foo", "this is foo", "some category");
		final org.cmdbuild.services.soap.Attachment bar = soapAttachment("bar", "this is bar", "some other category");
		doReturn(asList(foo, bar)) //
				.when(proxy()).getAttachmentList(anyString(), anyInt());
		final CardDescriptor descriptor = api().existingCard(CLASS_NAME, CARD_ID);

		// when
		selectedAttachments = api().existingCard(descriptor).attachments().selectAll();

		// then
		final Iterable<AttachmentDescriptor> selected = selectedAttachments.selected();
		assertThat(size(selected), equalTo(2));
		assertThat(get(selected, 0).getName(), equalTo("foo"));
		assertThat(get(selected, 1).getName(), equalTo("bar"));

		verify(proxy()).getAttachmentList(eq(CLASS_NAME), eq(CARD_ID));
		verifyNoMoreInteractions(proxy());
	}

	@Test
	public void someAttachmentsSelected() throws Exception {
		// given
		final org.cmdbuild.services.soap.Attachment foo = soapAttachment("foo", "this is foo", "some category");
		final org.cmdbuild.services.soap.Attachment bar = soapAttachment("bar", "this is bar", "some other category");
		final org.cmdbuild.services.soap.Attachment baz = soapAttachment("baz", "this is baz", "some category");
		doReturn(asList(foo, bar, baz)) //
				.when(proxy()).getAttachmentList(anyString(), anyInt());
		final CardDescriptor descriptor = api().existingCard(CLASS_NAME, CARD_ID);

		// when
		selectedAttachments = api().existingCard(descriptor) //
				.attachments() //
				.selectByName("foo", "baz");

		// then
		final Iterable<AttachmentDescriptor> selected = selectedAttachments.selected();
		assertThat(size(selected), equalTo(2));
		assertThat(get(selected, 0).getName(), equalTo("foo"));
		assertThat(get(selected, 1).getName(), equalTo("baz"));

		verify(proxy()).getAttachmentList(eq(CLASS_NAME), eq(CARD_ID));
		verifyNoMoreInteractions(proxy());
	}

	@Test
	public void attachmentsDownloaded() throws Exception {
		// given
		final org.cmdbuild.services.soap.Attachment foo = soapAttachment("foo", "this is foo", "some category");
		final org.cmdbuild.services.soap.Attachment bar = soapAttachment("bar", "this is bar", "some other category");
		final org.cmdbuild.services.soap.Attachment baz = soapAttachment("baz", "this is baz", "some category");
		doReturn(asList(foo, bar, baz)) //
				.when(proxy()).getAttachmentList(anyString(), anyInt());
		final DataHandler dataHandler = new DataHandler(new FileDataSource(temporaryFolder.newFile()));
		doReturn(dataHandler) //
				.when(proxy()).downloadAttachment(anyString(), anyInt(), anyString());
		final CardDescriptor descriptor = api().existingCard(CLASS_NAME, CARD_ID);

		// when
		final Iterable<Attachment> downloaded = api().existingCard(descriptor) //
				.attachments() //
				.selectByName("foo", "baz") //
				.download();

		// then
		assertThat(size(downloaded), equalTo(2));
		assertThat(get(downloaded, 0).getName(), equalTo("foo"));
		assertThat(get(downloaded, 1).getName(), equalTo("baz"));

		verify(proxy()).getAttachmentList(eq(CLASS_NAME), eq(CARD_ID));
		final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
		verify(proxy(), times(2)).downloadAttachment(eq(CLASS_NAME), eq(CARD_ID), fileNameCaptor.capture());
		verifyNoMoreInteractions(proxy());

		final List<String> values = fileNameCaptor.getAllValues();
		assertThat(values.size(), equalTo(2));
		assertThat(values.get(0), equalTo("foo"));
		assertThat(values.get(1), equalTo("baz"));
	}

	@Test
	public void attachmentsDeleted() throws Exception {
		// given
		final org.cmdbuild.services.soap.Attachment foo = soapAttachment("foo", "this is foo", "some category");
		final org.cmdbuild.services.soap.Attachment bar = soapAttachment("bar", "this is bar", "some other category");
		final org.cmdbuild.services.soap.Attachment baz = soapAttachment("baz", "this is baz", "some category");
		doReturn(asList(foo, bar, baz)) //
				.when(proxy()).getAttachmentList(anyString(), anyInt());
		final CardDescriptor descriptor = api().existingCard(CLASS_NAME, CARD_ID);

		// when
		api().existingCard(descriptor) //
				.attachments() //
				.selectByName("foo", "baz") //
				.delete();

		// then
		verify(proxy()).getAttachmentList(eq(CLASS_NAME), eq(CARD_ID));
		final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
		verify(proxy(), times(2)).deleteAttachment(eq(CLASS_NAME), eq(CARD_ID), fileNameCaptor.capture());
		verifyNoMoreInteractions(proxy());

		final List<String> values = fileNameCaptor.getAllValues();
		assertThat(values.size(), equalTo(2));
		assertThat(values.get(0), equalTo("foo"));
		assertThat(values.get(1), equalTo("baz"));
	}

	private static org.cmdbuild.services.soap.Attachment soapAttachment(final String _filename,
			final String _description, final String _category) {
		final org.cmdbuild.services.soap.Attachment foo = new org.cmdbuild.services.soap.Attachment() {
			{
				setFilename(_filename);
				setDescription(_description);
				setCategory(_category);
			}
		};
		return foo;
	}

}
