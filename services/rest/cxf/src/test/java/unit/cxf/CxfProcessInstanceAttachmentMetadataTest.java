package unit.cxf;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.util.Collections.emptyList;
import static org.cmdbuild.service.rest.model.Models.newAttachmentMetadata;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.cxf.CxfProcessInstanceAttachmentMetadata;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.model.AttachmentMetadata;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class CxfProcessInstanceAttachmentMetadataTest {

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

	private CxfProcessInstanceAttachmentMetadata cxfProcessInstanceAttachments;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		dmsLogic = mock(DmsLogic.class);
		workflowLogic = mock(WorkflowLogic.class);
		cxfProcessInstanceAttachments = new CxfProcessInstanceAttachmentMetadata(errorHandler, dmsLogic, workflowLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnRead() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));

		// when
		cxfProcessInstanceAttachments.read("foo", 123L, "bar");
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
		cxfProcessInstanceAttachments.read("foo", 123L, "bar");
	}

	@Test(expected = WebApplicationException.class)
	public void missingAttachmentIdOnRead() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("dummy class") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();

		// when
		cxfProcessInstanceAttachments.read("foo", 123L, null);
	}

	@Test(expected = WebApplicationException.class)
	public void blankAttachmentIdOnRead() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("dummy class") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();

		// when
		cxfProcessInstanceAttachments.read("foo", 123L, "");
	}

	@Test(expected = WebApplicationException.class)
	public void missingAttachmentOnRead() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("dummy class") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doReturn(absent()) //
				.when(dmsLogic).search(eq("foo"), eq(123L), eq("bar"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).attachmentNotFound("bar");

		// when
		cxfProcessInstanceAttachments.read("foo", 123L, "bar");
	}

	@Test
	public void logicCalledOnRead() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("dummy class") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final StoredDocument document = new StoredDocument();
		doReturn(of(document)) //
				.when(dmsLogic).search(eq("foo"), eq(123L), eq("bar"));

		// when
		cxfProcessInstanceAttachments.read("foo", 123L, "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).search(eq("foo"), eq(123L), eq("bar"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void missingClassOnUpdate() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("foo"));
		final AttachmentMetadata attachment = newAttachmentMetadata() //
				// ...
				.build();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", attachment);
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
		final AttachmentMetadata attachment = newAttachmentMetadata() //
				// ...
				.build();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", attachment);
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
		final AttachmentMetadata attachment = newAttachmentMetadata() //
				// ...
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, null, attachment);
	}

	@Test(expected = WebApplicationException.class)
	public void blankAttachmentIdOnUpdate() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("bar") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final AttachmentMetadata attachment = newAttachmentMetadata() //
				// ...
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentId();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "", attachment);
	}

	@Test(expected = WebApplicationException.class)
	public void missingAttachmentOnUpdate() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn("dummy class") //
				.when(targetClass).getName();
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance processInstance = mock(UserProcessInstance.class);
		doReturn(processInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingAttachmentMetadata();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", null);
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
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());
		final AttachmentMetadata attachment = newAttachmentMetadata() //
				.withId("the id") //
				.withName("the name") //
				.withCategory("the category") //
				.withDescription("the description") //
				.withAuthor("the author") //
				.withExtra(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz") //
						.chainablePut("baz", "foo")) //
				.build();

		// when
		cxfProcessInstanceAttachments.update("foo", 123L, "bar", attachment);

		// then
		final InOrder inOrder = inOrder(errorHandler, dmsLogic, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("foo"), eq(123L));
		inOrder.verify(dmsLogic).getCategoryDefinition(eq("the category"));
		inOrder.verify(dmsLogic).updateDescriptionAndMetadata(eq("foo"), eq(123L), eq("bar"), eq("the category"),
				eq("the description"), any(Iterable.class));
		inOrder.verifyNoMoreInteractions();
	}

}
