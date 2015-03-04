package unit.api.fluent;

import static com.google.common.collect.Iterables.get;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.api.fluent.ActiveQueryRelations;
import org.cmdbuild.api.fluent.Attachment;
import org.cmdbuild.api.fluent.AttachmentDescriptor;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.CreateReport;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.ExistingRelation;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.FluentApiExecutor.AdvanceProcess;
import org.cmdbuild.api.fluent.FunctionCall;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.api.fluent.NewProcessInstance;
import org.cmdbuild.api.fluent.NewRelation;
import org.cmdbuild.api.fluent.ProcessInstanceDescriptor;
import org.cmdbuild.api.fluent.QueryClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FluentApiTest {

	private static final String CLASS_NAME = "class";
	private static final String DOMAIN_NAME = "domain";
	private static final String FUNCTION_NAME = "function";
	private static final String REPORT_NAME = "report";
	private static final String REPORT_FORMAT = "xyz";
	private static final String PROCESS_CLASS_NAME = "processclass";

	private static final int CARD_ID = 42;
	private static final String PROCESS_INSTANCE_ID = "XYZ";

	private static final CardDescriptor CARD_DESCRIPTOR = new CardDescriptor(CLASS_NAME, CARD_ID);
	private static final ProcessInstanceDescriptor PROCESS_INSTANCE_DESCRIPTOR = new ProcessInstanceDescriptor(
			PROCESS_CLASS_NAME, CARD_ID, PROCESS_INSTANCE_ID);

	@Captor
	public ArgumentCaptor<Iterable<? extends AttachmentDescriptor>> captor;

	private FluentApiExecutor executor;
	private FluentApi api;

	@Before
	public void createApi() throws Exception {
		executor = mock(FluentApiExecutor.class);
		api = new FluentApi(executor);
	}

	@Test
	public void executorCalledWhenCreatingNewCard() {
		final NewCard newCard = api.newCard(CLASS_NAME);

		when(executor.create(newCard)).thenReturn(CARD_DESCRIPTOR);

		assertThat(newCard.create(), equalTo(CARD_DESCRIPTOR));

		verify(executor).create(newCard);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenUpdatingExistingCard() {
		final ExistingCard existingCard = api.existingCard(CLASS_NAME, CARD_ID);
		existingCard.update();

		verify(executor).update(existingCard);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenDeletingExistingCard() {
		final ExistingCard existingCard = api.existingCard(CLASS_NAME, CARD_ID);
		existingCard.delete();

		verify(executor).delete(existingCard);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenFetchingExistingCard() {
		final ExistingCard existingCard = api.existingCard(CLASS_NAME, CARD_ID);
		existingCard.fetch();

		verify(executor).fetch(existingCard);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenFetchingClass() {
		final QueryClass queryClass = api.queryClass(CLASS_NAME);
		queryClass.fetch();

		verify(executor).fetchCards(queryClass);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenCreatingNewRelation() {
		final NewRelation newRelation = api.newRelation(DOMAIN_NAME);
		newRelation.create();

		verify(executor).create(newRelation);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenDeletingExistingRelation() {
		final ExistingRelation existingRelation = api.existingRelation(DOMAIN_NAME);
		existingRelation.delete();

		verify(executor).delete(existingRelation);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenFetchingRelations() {
		final ActiveQueryRelations query = api.queryRelations(CLASS_NAME, CARD_ID);
		query.fetch();

		verify(executor).fetch(query);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenExecutingFunctionCall() {
		final FunctionCall functionCall = api.callFunction(FUNCTION_NAME);
		functionCall.execute();

		verify(executor).execute(functionCall);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenCreatingReport() {
		final CreateReport createReport = api.createReport(REPORT_NAME, REPORT_FORMAT);
		createReport.download();

		verify(executor).download(createReport);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenStartingNewProcessInstance() {
		final NewProcessInstance newProcess = api.newProcessInstance(PROCESS_CLASS_NAME);

		when(executor.createProcessInstance(newProcess, AdvanceProcess.NO)).thenReturn(PROCESS_INSTANCE_DESCRIPTOR);

		assertThat(newProcess.start(), sameInstance(PROCESS_INSTANCE_DESCRIPTOR));

		verify(executor).createProcessInstance(newProcess, AdvanceProcess.NO);
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenFetchingAttachments() {
		// given
		final CardDescriptor source = api.existingCard(CLASS_NAME, CARD_ID);
		final AttachmentDescriptor foo = mock(AttachmentDescriptor.class);
		final AttachmentDescriptor bar = mock(AttachmentDescriptor.class);
		final AttachmentDescriptor baz = mock(AttachmentDescriptor.class);
		final Iterable<AttachmentDescriptor> attachments = asList(foo, bar, baz);
		doReturn(attachments) //
				.when(executor).fetchAttachments(any(CardDescriptor.class));

		// when
		final Iterable<AttachmentDescriptor> fetched = api.existingCard(source) //
				.attachments() //
				.fetch();

		assertThat(fetched, equalTo(attachments));

		verify(executor).fetchAttachments(eq(source));
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenUploadingAttachments() {
		// given
		final CardDescriptor source = api.existingCard(CLASS_NAME, CARD_ID);
		final Attachment foo = mock(Attachment.class);
		final Attachment bar = mock(Attachment.class);
		final Attachment baz = mock(Attachment.class);

		// when
		api.existingCard(source) //
				.attachments() //
				.upload(foo, bar, baz);

		verify(executor).upload(eq(source), eq(asList(foo, bar, baz)));
		verifyNoMoreInteractions(executor);
	}

	@Test
	public void executorCalledWhenDeletingAttachments() {
		// given
		final CardDescriptor source = api.existingCard(CLASS_NAME, CARD_ID);
		final AttachmentDescriptor foo = mock(AttachmentDescriptor.class);
		final AttachmentDescriptor bar = mock(AttachmentDescriptor.class);
		final AttachmentDescriptor baz = mock(AttachmentDescriptor.class);
		final Iterable<AttachmentDescriptor> attachments = asList(foo, bar, baz);
		doReturn(attachments) //
				.when(executor).fetchAttachments(any(CardDescriptor.class));

		// when
		api.existingCard(source) //
				.attachments() //
				.selectAll() //
				.delete();

		verify(executor).fetchAttachments(eq(source));
		verify(executor).delete(eq(source), captor.capture());
		verifyNoMoreInteractions(executor);

		final Iterable<? extends AttachmentDescriptor> captured = captor.getValue();
		assertThat(get(captured, 0), equalTo(foo));
		assertThat(get(captured, 1), equalTo(bar));
		assertThat(get(captured, 2), equalTo(baz));
	}

	@Test
	public void executorCalledWhenDownloadingAttachments() {
		// given
		final CardDescriptor source = api.existingCard(CLASS_NAME, CARD_ID);
		final AttachmentDescriptor foo = mock(AttachmentDescriptor.class);
		final AttachmentDescriptor bar = mock(AttachmentDescriptor.class);
		final AttachmentDescriptor baz = mock(AttachmentDescriptor.class);
		final Iterable<AttachmentDescriptor> attachments = asList(foo, bar, baz);
		doReturn(attachments) //
				.when(executor).fetchAttachments(any(CardDescriptor.class));
		final Attachment downloaded_foo = mock(Attachment.class);
		final Attachment downloaded_bar = mock(Attachment.class);
		final Attachment downloaded_baz = mock(Attachment.class);
		final Iterable<Attachment> downloaded_attachments = asList(downloaded_foo, downloaded_bar, downloaded_baz);
		doReturn(downloaded_attachments) //
				.when(executor).download(any(CardDescriptor.class), any(Iterable.class));

		// when
		final Iterable<Attachment> downloaded = api.existingCard(source) //
				.attachments() //
				.selectAll() //
				.download();

		assertThat(get(downloaded, 0), equalTo(downloaded_foo));
		assertThat(get(downloaded, 1), equalTo(downloaded_bar));
		assertThat(get(downloaded, 2), equalTo(downloaded_baz));

		verify(executor).fetchAttachments(eq(source));
		verify(executor).download(eq(source), captor.capture());
		verifyNoMoreInteractions(executor);

		final Iterable<? extends AttachmentDescriptor> captured = captor.getValue();
		assertThat(get(captured, 0), equalTo(foo));
		assertThat(get(captured, 1), equalTo(bar));
		assertThat(get(captured, 2), equalTo(baz));
	}

	@Test
	public void executorCalledWhenCopyingAttachments() {
		// given
		final CardDescriptor source = api.existingCard(CLASS_NAME, CARD_ID);
		final AttachmentDescriptor foo = mock(AttachmentDescriptor.class);
		final AttachmentDescriptor bar = mock(AttachmentDescriptor.class);
		final AttachmentDescriptor baz = mock(AttachmentDescriptor.class);
		final Iterable<AttachmentDescriptor> attachments = asList(foo, bar, baz);
		doReturn(attachments) //
				.when(executor).fetchAttachments(any(CardDescriptor.class));
		final CardDescriptor destination = api.existingCard(CLASS_NAME + "2", CARD_ID * 10);

		// when
		api.existingCard(source) //
				.attachments() //
				.selectAll() //
				.copyTo(destination);

		verify(executor).fetchAttachments(eq(source));
		verify(executor).copy(eq(source), captor.capture(), eq(destination));
		verifyNoMoreInteractions(executor);

		final Iterable<? extends AttachmentDescriptor> captured = captor.getValue();
		assertThat(get(captured, 0), equalTo(foo));
		assertThat(get(captured, 1), equalTo(bar));
		assertThat(get(captured, 2), equalTo(baz));
	}

}
