package unit.cxf;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Models.newAttachment;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.NoSuchElementException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.io.input.NullInputStream;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.cxf.AttachmentsHelper;
import org.cmdbuild.service.rest.cxf.CxfProcessInstanceAttachments;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InOrder;

public class CxfProcessInstanceAttachmentsTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private ErrorHandler errorHandler;
	private WorkflowLogic workflowLogic;
	private AttachmentsHelper attachmentsHelper;

	private CxfProcessInstanceAttachments cxfProcessInstanceAttachments;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		workflowLogic = mock(WorkflowLogic.class);
		attachmentsHelper = mock(AttachmentsHelper.class);
		cxfProcessInstanceAttachments = new CxfProcessInstanceAttachments(errorHandler, workflowLogic,
				attachmentsHelper);
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnCreate() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));
		final Attachment attachment = newAttachment().build();
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfProcessInstanceAttachments.create("foo", 123L, attachment, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingCardOnCreate() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new NoSuchElementException()) //
				.when(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(123L));
		final Attachment attachment = newAttachment().build();
		final DataHandler dataHandler = dataHandler();

		// when
		cxfProcessInstanceAttachments.create("foo", 123L, attachment, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingFileOnCreate() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingFile();
		final Attachment attachment = newAttachment().build();

		// when
		cxfProcessInstanceAttachments.create("foo", 123L, attachment, null);
	}

	@Test(expected = WebApplicationException.class)
	public void alreadyExistingFileNameOnCreate() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final DataHandler dataHandler = dataHandler("already existing");
		doReturn(asList( //
				newAttachment() //
						.withName("already existing") //
						.build(), //
				newAttachment() //
						.withName("yet another already existing") //
						.build())) //
				.when(attachmentsHelper).search(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).alreadyExistingAttachmentName(eq("already existing"));

		// when
		cxfProcessInstanceAttachments.create("foo", 123L, null, dataHandler);
	}

	@Test
	public void logicCalledOnCreateWithBothAttachmentAndFile() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final Attachment attachment = newAttachment() //
				.withCategory("the category") //
				.withDescription("the description") //
				.build();
		final DataHandler dataHandler = dataHandler("file name");
		doReturn(asList( //
				newAttachment() //
						.withName("file name") //
						.build())) //
				.when(attachmentsHelper).search(anyString(), anyLong());
		doReturn("bar") //
				.when(attachmentsHelper).create(anyString(), anyLong(), anyString(), any(Attachment.class),
						any(DataHandler.class));

		// when
		final ResponseSingle<String> response = cxfProcessInstanceAttachments.create("foo", 123L, attachment,
				dataHandler);

		// then
		assertThat(response.getElement(), equalTo("bar"));
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, attachmentsHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).search(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).create(eq("foo"), eq(123L), eq("file name"), eq(attachment), eq(dataHandler));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledOnCreateWithFileOnly() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final DataHandler dataHandler = dataHandler("file name");
		doReturn(asList( //
				newAttachment() //
						.withName("file name") //
						.build())) //
				.when(attachmentsHelper).search(anyString(), anyLong());
		doReturn("bar") //
				.when(attachmentsHelper).create(anyString(), anyLong(), anyString(), any(Attachment.class),
						any(DataHandler.class));

		// when
		final ResponseSingle<String> response = cxfProcessInstanceAttachments.create("foo", 123L, null, dataHandler);

		// then
		assertThat(response.getElement(), equalTo("bar"));
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, attachmentsHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).search(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).create(eq("foo"), eq(123L), eq("file name"), isNull(Attachment.class),
				eq(dataHandler));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnReadAll() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));

		// when
		cxfProcessInstanceAttachments.read("foo", 123L);
	}

	@Test(expected = WebApplicationException.class)
	public void missingCardOnReadAll() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new NoSuchElementException()) //
				.when(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(123L));

		// when
		cxfProcessInstanceAttachments.read("foo", 123L);
	}

	@Test
	public void logicCalledOnReadAll() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final Collection<Attachment> attachments = asList( //
				newAttachment() //
						.withId("foo").build(), //
				newAttachment() //
						.withId("bar").build());
		doReturn(attachments) //
				.when(attachmentsHelper).search(anyString(), anyLong());

		// when
		final ResponseMultiple<Attachment> response = cxfProcessInstanceAttachments.read("foo", 123L);

		// then
		assertThat(response.getElements(), equalTo(attachments));
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, attachmentsHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).search(eq("foo"), eq(123L));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnRead() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));

		// when
		cxfProcessInstanceAttachments.download("foo", 123L, "bar");
	}

	@Test(expected = WebApplicationException.class)
	public void missingCardOnRead() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new NoSuchElementException()) //
				.when(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(123L));

		// when
		cxfProcessInstanceAttachments.download("foo", 123L, "bar");
	}

	@Test
	public void logicCalledOnRead() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final DataHandler dataHandler = dataHandler();
		doReturn(dataHandler) //
				.when(attachmentsHelper).download(anyString(), anyLong(), anyString());

		// when
		final DataHandler response = cxfProcessInstanceAttachments.download("foo", 123L, "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, attachmentsHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).download(eq("foo"), eq(123L), eq("bar"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnUpdate() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));
		final Attachment attachment = newAttachment().build();
		final DataHandler dataHandler = dataHandler();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", attachment, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingCardOnUpdate() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new NoSuchElementException()) //
				.when(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(123L));
		final Attachment attachment = newAttachment().build();
		final DataHandler dataHandler = dataHandler();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", attachment, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingAttachmentIdOnUpdate() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();
		final Attachment attachment = newAttachment().build();
		final DataHandler dataHandler = dataHandler();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, null, attachment, dataHandler);
	}

	@Test
	public void logicCalledOnUpdateWithBothAttachmentAndFile() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final Attachment attachment = newAttachment() //
				.withCategory("the new category") //
				.withDescription("the new description") //
				.build();
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = dataHandler();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", attachment, dataHandler);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, attachmentsHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).update(eq("foo"), eq(123L), eq("bar"), eq(attachment), eq(dataHandler));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledOnUpdateWithFileOnly() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = dataHandler();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", null, dataHandler);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, attachmentsHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).update(eq("foo"), eq(123L), eq("bar"), isNull(Attachment.class),
				eq(dataHandler));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledOnUpdateWithAttachmentOnly() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final Attachment attachment = newAttachment() //
				.withCategory("the new category") //
				.withDescription("the new description") //
				.build();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", attachment, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, attachmentsHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).update(eq("foo"), eq(123L), eq("bar"), eq(attachment),
				isNull(DataHandler.class));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicNotCalledOnUpdateWhenBothAttachmentAndFileAreMissing() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", null, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, attachmentsHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).update(eq("foo"), eq(123L), eq("bar"), isNull(Attachment.class),
				isNull(DataHandler.class));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnDelete() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));

		// when
		cxfProcessInstanceAttachments.delete("foo", 123L, "bar");
	}

	@Test(expected = WebApplicationException.class)
	public void missingCardOnDelete() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("bar");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new NoSuchElementException()) //
				.when(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(123L));

		// when
		cxfProcessInstanceAttachments.delete("foo", 123L, "bar");
	}

	@Test
	public void logicCalledOnDelete() throws Exception {
		// given
		final UserProcessClass targetClass = mockClass("baz");
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());

		// when
		cxfProcessInstanceAttachments.delete("foo", 123L, "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, attachmentsHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(attachmentsHelper).delete(eq("foo"), eq(123L), eq("bar"));
		inOrder.verifyNoMoreInteractions();
	}

	private UserProcessClass mockClass(final String name) {
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn(name) //
				.when(targetClass).getName();
		return targetClass;
	}

	private DataHandler dataHandler(final String fileName) throws IOException {
		return dataHandler(temporaryFolder.newFile(fileName));
	}

	private DataHandler dataHandler() throws IOException {
		return dataHandler(temporaryFolder.newFile());
	}

	private DataHandler dataHandler(final File file) throws IOException {
		final DataHandler dataHandler = new DataHandler(new FileDataSource(file));
		return dataHandler;
	}

}
