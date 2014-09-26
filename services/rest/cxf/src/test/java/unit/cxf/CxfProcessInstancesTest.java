package unit.cxf;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.transformEntries;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.cxf.CxfProcessInstances;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.cxf.util.Maps;
import org.cmdbuild.service.rest.model.ProcessInstance;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class CxfProcessInstancesTest {

	private static final Map<String, Object> NO_WIDGETS = Collections.emptyMap();

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
	public void exceptionWhenCreatingInstanceButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcessInstances.create("foo", null, false);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(errorHandler).processNotFound("foo");
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenCreatingInstanceButBusinessLogicThrowsException() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final MetadataMap<String, String> formParam = new MetadataMap<String, String>();
		ChainablePutMap.of(formParam) //
				.chainablePut("foo", asList("oof")) //
				.chainablePut("bar", asList("rab")) //
				.chainablePut("baz", asList("zab"));
		final Map<String, String> vars = transformEntries(formParam, Maps.<String, String> firstElement());
		final CMWorkflowException workflowException = new CMWorkflowException("error");
		doThrow(workflowException) //
				.when(workflowLogic).startProcess(anyString(), anyMapOf(String.class, String.class),
						anyMapOf(String.class, Object.class), anyBoolean());
		doThrow(new WebApplicationException(workflowException)) //
				.when(errorHandler).propagate(workflowException);

		// when
		cxfProcessInstances.create("foo", formParam, true);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(workflowLogic).startProcess("foo", vars, NO_WIDGETS, true);
		inOrder.verify(errorHandler).propagate(workflowException);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void businessLogicCalledSuccessfullyWhenCreatingInstance() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final MetadataMap<String, String> formParam = new MetadataMap<String, String>();
		ChainablePutMap.of(formParam) //
				.chainablePut("foo", asList("oof")) //
				.chainablePut("bar", asList("rab")) //
				.chainablePut("baz", asList("zab"));
		final Map<String, String> vars = transformEntries(formParam, Maps.<String, String> firstElement());
		final UserProcessInstance userProcessInstance = mock(UserProcessInstance.class);
		doReturn(123L) //
				.when(userProcessInstance).getId();
		doReturn(userProcessInstance) //
				.when(workflowLogic).startProcess(anyString(), anyMapOf(String.class, String.class),
						anyMapOf(String.class, Object.class), anyBoolean());

		// when
		final ResponseSingle<Long> response = cxfProcessInstances.create("foo", formParam, true);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(workflowLogic).startProcess("foo", vars, NO_WIDGETS, true);
		inOrder.verifyNoMoreInteractions();
		assertThat(response.getElement(), equalTo(123L));
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingAllInstancesButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcessInstances.read("foo", null, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(errorHandler).processNotFound("foo");
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void allInstancesReturned() throws Exception {
		// given
		final CMAttribute attribute = mock(CMAttribute.class);
		doReturn(new StringAttributeType()) //
				.when(attribute).getType();
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn("type") //
				.when(userProcessClass).getName();
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doReturn(attribute) //
				.when(userProcessClass).getAttribute(anyString());
		final UserProcessInstance foo = mock(UserProcessInstance.class);
		doReturn(userProcessClass) //
				.when(foo).getType();
		doReturn(123L) //
				.when(foo).getId();
		doReturn("foo") //
				.when(foo).getProcessInstanceId();
		doReturn(ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz") //
				.entrySet()) //
				.when(foo).getAllValues();
		final UserProcessInstance bar = mock(UserProcessInstance.class);
		doReturn(userProcessClass) //
				.when(bar).getType();
		doReturn(456L) //
				.when(bar).getId();
		doReturn("bar") //
				.when(bar).getProcessInstanceId();
		doReturn(ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("bar", "baz") //
				.chainablePut("baz", "foo") //
				.entrySet()) //
				.when(bar).getAllValues();
		final PagedElements<UserProcessInstance> pagedElements = new PagedElements<UserProcessInstance>(
				asList(foo, bar), 4);
		doReturn(pagedElements) //
				.when(workflowLogic).query(anyString(), any(QueryOptions.class));

		// when
		final ResponseMultiple<ProcessInstance> response = cxfProcessInstances.read("foo", null, null);

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

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingInstanceButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound("foo");

		// when
		cxfProcessInstances.read("foo", 123L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(errorHandler).processNotFound("foo");
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingInstanceButInstanceNotFound() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final PagedElements<UserProcessInstance> noElements = new PagedElements<UserProcessInstance>(null, 0);
		doReturn(noElements) //
				.when(workflowLogic).query(anyString(), any(QueryOptions.class));
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processInstanceNotFound(anyLong());

		// when
		cxfProcessInstances.read("foo", 123L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(workflowLogic).query(eq("foo"), any(QueryOptions.class));
		inOrder.verify(errorHandler).processInstanceNotFound(123L);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void singleInstanceReturned() throws Exception {
		// given
		final CMAttribute attribute = mock(CMAttribute.class);
		doReturn(new StringAttributeType()) //
				.when(attribute).getType();
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn("type") //
				.when(userProcessClass).getName();
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doReturn(attribute) //
				.when(userProcessClass).getAttribute(anyString());
		final UserProcessInstance instance = mock(UserProcessInstance.class);
		doReturn(userProcessClass) //
				.when(instance).getType();
		doReturn(123L) //
				.when(instance).getId();
		doReturn("foo") //
				.when(instance).getProcessInstanceId();
		doReturn(ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz") //
				.entrySet()) //
				.when(instance).getAllValues();
		final PagedElements<UserProcessInstance> pagedElements = new PagedElements<UserProcessInstance>(
				asList(instance), 1);
		doReturn(pagedElements) //
				.when(workflowLogic).query(anyString(), any(QueryOptions.class));

		// when
		final ResponseSingle<ProcessInstance> response = cxfProcessInstances.read("foo", 123L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(workflowLogic).query(eq("foo"), any(QueryOptions.class));
		inOrder.verifyNoMoreInteractions();
		final ProcessInstance element = response.getElement();
		assertThat(element.getType(), equalTo("type"));
		assertThat(element.getId(), equalTo(123L));
		assertThat(element.getName(), equalTo("foo"));
		assertThat(element.getValues(), hasEntry("foo", (Object) "bar"));
		assertThat(element.getValues(), hasEntry("bar", (Object) "baz"));
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenUpdatingInstanceButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcessInstances.update("foo", 123L, "bar", true, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(errorHandler).processNotFound("foo");
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenUpdatingInstanceButBusinessLogicThrowsException() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final MetadataMap<String, String> formParam = new MetadataMap<String, String>();
		ChainablePutMap.of(formParam) //
				.chainablePut("foo", asList("oof")) //
				.chainablePut("bar", asList("rab")) //
				.chainablePut("baz", asList("zab"));
		final Map<String, String> vars = transformEntries(formParam, Maps.<String, String> firstElement());
		final CMWorkflowException workflowException = new CMWorkflowException("error");
		doThrow(workflowException) //
				.when(workflowLogic).updateProcess(anyString(), anyLong(), anyString(),
						anyMapOf(String.class, String.class), anyMapOf(String.class, Object.class), anyBoolean());
		doThrow(new WebApplicationException(workflowException)) //
				.when(errorHandler).propagate(workflowException);

		// when
		cxfProcessInstances.update("foo", 123L, "bar", true, formParam);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(workflowLogic).updateProcess("foo", 123L, "bar", vars, NO_WIDGETS, true);
		inOrder.verify(errorHandler).propagate(workflowException);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void businessLogicCalledSuccessfullyWhenUpdatingInstance() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final MetadataMap<String, String> formParam = new MetadataMap<String, String>();
		ChainablePutMap.of(formParam) //
				.chainablePut("foo", asList("oof")) //
				.chainablePut("bar", asList("rab")) //
				.chainablePut("baz", asList("zab"));
		final Map<String, String> vars = transformEntries(formParam, Maps.<String, String> firstElement());
		final UserProcessInstance userProcessInstance = mock(UserProcessInstance.class);
		doReturn(123L) //
				.when(userProcessInstance).getId();
		doReturn(userProcessInstance) //
				.when(workflowLogic).startProcess(anyString(), anyMapOf(String.class, String.class),
						anyMapOf(String.class, Object.class), anyBoolean());

		// when
		cxfProcessInstances.update("foo", 123L, "bar", true, formParam);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(workflowLogic).updateProcess("foo", 123L, "bar", vars, NO_WIDGETS, true);
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenDeletingInstanceButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcessInstances.delete("foo", 123L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(errorHandler).processNotFound("foo");
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenDeletingInstanceButBusinessLogicThrowsException() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final CMWorkflowException workflowException = new CMWorkflowException("error");
		doThrow(workflowException) //
				.when(workflowLogic).abortProcess(anyString(), anyLong());
		doThrow(new WebApplicationException(workflowException)) //
				.when(errorHandler).propagate(workflowException);

		// when
		cxfProcessInstances.delete("foo", 123L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(workflowLogic).abortProcess("foo", 123L);
		inOrder.verify(errorHandler).propagate(workflowException);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void businessLogicCalledSuccessfullyWhenDeletingInstance() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());

		// when
		cxfProcessInstances.delete("foo", 123L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass("foo");
		inOrder.verify(workflowLogic).abortProcess("foo", 123L);
		inOrder.verifyNoMoreInteractions();
	}

}
