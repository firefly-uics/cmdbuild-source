package unit.cxf;

import static java.util.Collections.emptyList;
import static org.cmdbuild.service.rest.model.Models.newAttachment;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.NoSuchElementException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.io.input.NullInputStream;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.cxf.CxfProcessInstanceAttachments;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class CxfProcessInstanceAttachmentsTest {

	protected static final Iterable<MetadataGroupDefinition> NO_METADATA_GROUP_DEFINITION = emptyList();

	private static final DocumentTypeDefinition NO_METADATA = new DocumentTypeDefinition() {

		@Override
		public String getName() {
			return "dummy " + DocumentTypeDefinition.class;
		};

		@Override
		public Iterable<MetadataGroupDefinition> getMetadataGroupDefinitions() {
			return NO_METADATA_GROUP_DEFINITION;
		};

	};

	private ErrorHandler errorHandler;
	private DmsLogic dmsLogic;
	private WorkflowLogic workflowLogic;
	private UserStore userStore;

	private CxfProcessInstanceAttachments cxfProcessInstanceAttachments;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		dmsLogic = mock(DmsLogic.class);
		workflowLogic = mock(WorkflowLogic.class);
		userStore = mock(UserStore.class);
		cxfProcessInstanceAttachments = new CxfProcessInstanceAttachments(errorHandler, dmsLogic, workflowLogic,
				userStore);

		final AuthenticatedUser authUser = mock(AuthenticatedUser.class);
		doReturn("dummy user") //
				.when(authUser).getUsername();
		final OperationUser operationUser = new OperationUser(authUser, new NullPrivilegeContext(), new NullGroup());
		doReturn(operationUser) //
				.when(userStore).getUser();
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
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfProcessInstanceAttachments.create("foo", 123L, attachment, dataHandler);
	}

	@Test
	public void missingAttachmentIsNotAProblemOnCreate() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentName();
		final DataSource dataSource = mock(DataSource.class);
		doReturn("file name") //
				.when(dataSource).getName();
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfProcessInstanceAttachments.create("foo", 123L, null, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingFileOnCreate() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
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

	@Test
	public void logicCalledOnCreate() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final Attachment attachment = newAttachment() //
				.withCategory("the category") //
				.withDescription("the description") //
				.build();
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn("file name") //
				.when(dataSource).getName();
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = new DataHandler(dataSource);
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		cxfProcessInstanceAttachments.create("foo", 123L, attachment, dataHandler);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, workflowLogic, userStore);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(userStore).getUser();
		inOrder.verify(dmsLogic).upload(eq("dummy user"), eq("foo"), eq(123L), same(inputStream), eq("file name"),
				eq("the category"), eq("the description"), any(Iterable.class));
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
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());

		// when
		cxfProcessInstanceAttachments.read("foo", 123L);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, workflowLogic, userStore);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).search(eq("foo"), eq(123L));
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
		final UserProcessClass targetClass = mock(UserProcessClass.class);
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
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());

		// when
		cxfProcessInstanceAttachments.download("foo", 123L, "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, workflowLogic, userStore);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).download(eq("foo"), eq(123L), eq("bar"));
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
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", attachment, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingCardOnUpdate() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new NoSuchElementException()) //
				.when(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(123L));
		final Attachment attachment = newAttachment().build();
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", attachment, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingAttachmentIdOnUpdate() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();
		final Attachment attachment = newAttachment().build();
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, null, attachment, dataHandler);
	}

	@Test
	public void missingAttachmentIsNotAProblemOnUpdate() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();
		final DataSource dataSource = mock(DataSource.class);
		final DataHandler dataHandler = new DataHandler(dataSource);

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", null, dataHandler);
	}

	@Test(expected = WebApplicationException.class)
	public void missingFileOnUpdate() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingFile();
		final Attachment attachment = newAttachment().build();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", attachment, null);
	}

	@Test
	public void logicCalledOnUpdate() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
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
		final DataHandler dataHandler = new DataHandler(dataSource);
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", attachment, dataHandler);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, workflowLogic, userStore);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(userStore).getUser();
		inOrder.verify(dmsLogic).upload(eq("dummy user"), eq("foo"), eq(123L), same(inputStream), eq("bar"),
				eq("the new category"), eq("the new description"), any(Iterable.class));
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
		final UserProcessClass targetClass = mock(UserProcessClass.class);
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
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("baz") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());

		// when
		cxfProcessInstanceAttachments.delete("foo", 123L, "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, workflowLogic, userStore);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).delete(eq("foo"), eq(123L), eq("bar"));
		inOrder.verifyNoMoreInteractions();
	}

}
