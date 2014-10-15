package unit.cxf;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.cxf.CxfProcessStartActivity;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails.AttributeStatus;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class CxfProcessStartActivityTest {

	private ErrorHandler errorHandler;
	private WorkflowLogic workflowLogic;

	private CxfProcessStartActivity cxfProcessStartActivity;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		workflowLogic = mock(WorkflowLogic.class);
		cxfProcessStartActivity = new CxfProcessStartActivity(errorHandler, workflowLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcessStartActivity.read("123");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(errorHandler).processNotFound(eq("123"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionIfLogicThrowsException() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(workflowLogic).getStartActivity(anyString());

		// when
		cxfProcessStartActivity.read("123");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).getStartActivity(eq("123"));
		inOrder.verify(errorHandler).propagate(any(Throwable.class));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void startActivityReturned() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final CMActivity activity = mock(CMActivity.class);
		doReturn("id") //
				.when(activity).getId();
		doReturn("description") //
				.when(activity).getDescription();
		doReturn("instructions") //
				.when(activity).getInstructions();
		doReturn(asList( //
				new CMActivityVariableToProcess("foo", Type.READ_ONLY), //
				new CMActivityVariableToProcess("bar", Type.READ_WRITE), //
				new CMActivityVariableToProcess("baz", Type.READ_WRITE_REQUIRED) //
				)) //
				.when(activity).getVariables();
		doReturn(activity) //
				.when(workflowLogic).getStartActivity(anyString());

		// when
		final ResponseSingle<ProcessActivityWithFullDetails> response = cxfProcessStartActivity.read("123");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).getStartActivity(eq("123"));
		inOrder.verifyNoMoreInteractions();

		final ProcessActivityWithFullDetails element = response.getElement();
		assertThat(element.getId(), equalTo(activity.getId()));
		assertThat(element.getDescription(), equalTo(activity.getDescription()));
		assertThat(element.getInstructions(), equalTo(activity.getInstructions()));

		final Iterable<AttributeStatus> attributes = element.getAttributes();
		assertThat(size(attributes), equalTo(3));

		final AttributeStatus fooReadOnly = get(attributes, 0);
		assertThat(fooReadOnly.getId(), equalTo("foo"));
		assertThat(fooReadOnly.isWritable(), equalTo(false));
		assertThat(fooReadOnly.isMandatory(), equalTo(false));

		final AttributeStatus barWriteableAndNotMandatory = get(attributes, 1);
		assertThat(barWriteableAndNotMandatory.getId(), equalTo("bar"));
		assertThat(barWriteableAndNotMandatory.isWritable(), equalTo(true));
		assertThat(barWriteableAndNotMandatory.isMandatory(), equalTo(false));

		final AttributeStatus bazWriteableAndMandatory = get(attributes, 2);
		assertThat(bazWriteableAndMandatory.getId(), equalTo("baz"));
		assertThat(bazWriteableAndMandatory.isWritable(), equalTo(true));
		assertThat(bazWriteableAndMandatory.isMandatory(), equalTo(true));
	}

}
