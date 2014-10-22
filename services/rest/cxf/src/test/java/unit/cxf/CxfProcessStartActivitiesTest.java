package unit.cxf;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.cxf.CxfProcessStartActivities;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails.AttributeStatus;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class CxfProcessStartActivitiesTest {

	private ErrorHandler errorHandler;
	private WorkflowLogic workflowLogic;

	private CxfProcessStartActivities cxfProcessStartActivities;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		workflowLogic = mock(WorkflowLogic.class);
		cxfProcessStartActivities = new CxfProcessStartActivities(errorHandler, workflowLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void processNotFoundWhileReadingAll() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(eq("foo"));

		// when
		cxfProcessStartActivities.read("foo");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
	}

	@Test(expected = WebApplicationException.class)
	public void logicThrowsExceptionWhileReadingAll() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(workflowLogic).getStartActivity(anyString());

		// when
		cxfProcessStartActivities.read("123");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).getStartActivity(eq("123"));
	}

	@Test
	public void startActivitiesReturned() throws Exception {
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
		final ResponseMultiple<ProcessActivityWithBasicDetails> response = cxfProcessStartActivities.read("123");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).getStartActivity(eq("123"));
		inOrder.verifyNoMoreInteractions();

		final Collection<ProcessActivityWithBasicDetails> elements = response.getElements();
		assertThat(elements.size(), equalTo(1));

		final ProcessActivityWithBasicDetails element = elements.iterator().next();
		assertThat(element.getId(), equalTo(activity.getId()));
		assertThat(element.getDescription(), equalTo(activity.getDescription()));
	}

	@Test(expected = WebApplicationException.class)
	public void processNotFoundWhileReadingDetail() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(eq("foo"));

		// when
		cxfProcessStartActivities.read("foo", "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
	}

	@Test(expected = WebApplicationException.class)
	public void activityNotFoundWhileReadingDetail() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final CMActivity activity = mock(CMActivity.class);
		doReturn("baz") //
				.when(activity).getId();
		doReturn(activity) //
				.when(workflowLogic).getStartActivity(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processActivityNotFound(eq("bar"));

		// when
		cxfProcessStartActivities.read("foo", "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getStartActivity(eq("foo"));
	}

	@Test
	public void startActivityDetailReturned() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final CMActivity activity = mock(CMActivity.class);
		doReturn("bar") //
				.when(activity).getId();
		doReturn("description") //
				.when(activity).getDescription();
		doReturn("instructions") //
				.when(activity).getInstructions();
		doReturn(asList( //
				new CMActivityVariableToProcess("first", Type.READ_ONLY), //
				new CMActivityVariableToProcess("second", Type.READ_WRITE), //
				new CMActivityVariableToProcess("third", Type.READ_WRITE_REQUIRED) //
				)) //
				.when(activity).getVariables();
		doReturn(activity) //
				.when(workflowLogic).getStartActivity(anyString());

		// when
		final ResponseSingle<ProcessActivityWithFullDetails> response = cxfProcessStartActivities.read("foo", "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("foo"));
		inOrder.verify(workflowLogic).getStartActivity(eq("foo"));
		inOrder.verifyNoMoreInteractions();

		final ProcessActivityWithFullDetails element = response.getElement();

		assertThat(element.getId(), equalTo(activity.getId()));
		assertThat(element.getDescription(), equalTo(activity.getDescription()));
		assertThat(element.getInstructions(), equalTo(activity.getInstructions()));

		final Iterable<AttributeStatus> attributes = element.getAttributes();
		assertThat(size(attributes), equalTo(3));

		final AttributeStatus fooReadOnly = get(attributes, 0);
		assertThat(fooReadOnly.getId(), equalTo("first"));
		assertThat(fooReadOnly.isWritable(), equalTo(false));
		assertThat(fooReadOnly.isMandatory(), equalTo(false));

		final AttributeStatus barWriteableAndNotMandatory = get(attributes, 1);
		assertThat(barWriteableAndNotMandatory.getId(), equalTo("second"));
		assertThat(barWriteableAndNotMandatory.isWritable(), equalTo(true));
		assertThat(barWriteableAndNotMandatory.isMandatory(), equalTo(false));

		final AttributeStatus bazWriteableAndMandatory = get(attributes, 2);
		assertThat(bazWriteableAndMandatory.getId(), equalTo("third"));
		assertThat(bazWriteableAndMandatory.isWritable(), equalTo(true));
		assertThat(bazWriteableAndMandatory.isMandatory(), equalTo(true));
	}

}