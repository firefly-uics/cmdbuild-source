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

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.cxf.CxfProcessInstances;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessInstance;
import org.cmdbuild.service.rest.serialization.ErrorHandler;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class CxfProcessInstancesTest {

	private ErrorHandler errorHandler;
	private WorkflowLogic workflowLogic;

	private CxfProcessInstances cxfProcessInstances;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		workflowLogic = mock(WorkflowLogic.class);
		cxfProcessInstances = new CxfProcessInstances(errorHandler, workflowLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).classNotFound("foo");

		// when
		cxfProcessInstances.readAll("foo", null, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(errorHandler).classNotFound("foo");
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void allInstancesReturned() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance foo = mock(UserProcessInstance.class);
		doReturn("foo").when(foo).getProcessInstanceId();
		final UserProcessInstance bar = mock(UserProcessInstance.class);
		doReturn("bar").when(bar).getProcessInstanceId();
		final PagedElements<UserProcessInstance> pagedElements = new PagedElements<UserProcessInstance>(
				asList(foo, bar), 4);
		doReturn(pagedElements) //
				.when(workflowLogic).query(anyString(), any(QueryOptions.class));

		// when
		final ListResponse<ProcessInstance> response = cxfProcessInstances.readAll("foo", null, null);

		// then
		final ArgumentCaptor<QueryOptions> queryOptionsCaptor = ArgumentCaptor.forClass(QueryOptions.class);
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(workflowLogic).query(eq("foo"), queryOptionsCaptor.capture());
		inOrder.verifyNoMoreInteractions();
		final QueryOptions captured = queryOptionsCaptor.getValue();
		assertThat(captured.getLimit(), equalTo(Integer.MAX_VALUE));
		assertThat(captured.getOffset(), equalTo(0));
		assertThat(response.getMetadata().getTotal(), equalTo(4L));
		final Iterable<ProcessInstance> elements = response.getElements();
		assertThat(size(elements), equalTo(2));
		assertThat(get(elements, 0).getName(), equalTo("foo"));
		assertThat(get(elements, 1).getName(), equalTo("bar"));
	}

}
