package unit.cxf;

import static org.cmdbuild.service.rest.model.Builders.newProcessInstance;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.service.rest.ProcessInstances;
import org.cmdbuild.service.rest.cxf.CxfInstances;
import org.cmdbuild.service.rest.model.ProcessInstance;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.Test;

public class CxfInstancesTest {

	private ProcessInstances processInstances;

	private CxfInstances cxfInstances;

	@Before
	public void setUp() throws Exception {
		processInstances = mock(ProcessInstances.class);
		cxfInstances = new CxfInstances(processInstances);
	}

	@Test
	public void createDelegatedCorrectly() throws Exception {
		// given
		final MultivaluedMap<String, String> formParams = mock(MultivaluedMap.class);
		final ResponseSingle<Long> expectedResponse = newResponseSingle(Long.class) //
				.withElement(123L) //
				.build();
		doReturn(expectedResponse) //
				.when(processInstances).create(anyString(), any(MultivaluedMap.class), anyBoolean());

		// when
		final ResponseSingle<Long> response = cxfInstances.create(formParams, "foo", true);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(processInstances).create("foo", formParams, true);
		verifyNoMoreInteractions(processInstances);
	}

	@Test
	public void readSingleDelegatedCorrectly() throws Exception {
		// given
		final ResponseSingle<ProcessInstance> expectedResponse = newResponseSingle(ProcessInstance.class) //
				.withElement(newProcessInstance() //
						// not important
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(processInstances).read(anyString(), anyLong());

		// when
		final ResponseSingle<ProcessInstance> response = cxfInstances.read(123L, "foo");

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(processInstances).read("foo", 123L);
		verifyNoMoreInteractions(processInstances);
	}

	@Test
	public void readAllDelegatedCorrectly() throws Exception {
		// given
		final ResponseMultiple<ProcessInstance> expectedResponse = newResponseMultiple(ProcessInstance.class) //
				.withElement(newProcessInstance() //
						// not important
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(processInstances).read(anyString(), anyInt(), anyInt());

		// when
		final ResponseMultiple<ProcessInstance> response = cxfInstances.read("foo", 123, 456);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(processInstances).read("foo", 123, 456);
		verifyNoMoreInteractions(processInstances);
	}

	@Test
	public void updateDelegatedCorrectly() throws Exception {
		// given
		final MultivaluedMap<String, String> formParams = mock(MultivaluedMap.class);

		// when
		cxfInstances.update(123L, "foo", "bar", true, formParams);

		// then
		verify(processInstances).update("foo", 123L, "bar", true, formParams);
		verifyNoMoreInteractions(processInstances);
	}

	@Test
	public void deleteDelegatedCorrectly() throws Exception {
		// when
		cxfInstances.delete(123L, "foo");

		// then
		verify(processInstances).delete("foo", 123L);
		verifyNoMoreInteractions(processInstances);
	}

}
