package unit.cxf;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.service.rest.ProcessInstanceActivities;
import org.cmdbuild.service.rest.cxf.CxfActivities;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessActivity;
import org.cmdbuild.service.rest.dto.ProcessActivityDefinition;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.junit.Before;
import org.junit.Test;

public class CxfActivitiesTest {

	private ProcessInstanceActivities processInstanceActivities;

	private CxfActivities cxfActivities;

	@Before
	public void setUp() throws Exception {
		processInstanceActivities = mock(ProcessInstanceActivities.class);
		cxfActivities = new CxfActivities(processInstanceActivities);
	}

	@Test
	public void readSingleDelegatedCorrectly() throws Exception {
		// given
		final SimpleResponse<ProcessActivityDefinition> expectedResponse = SimpleResponse
				.newInstance(ProcessActivityDefinition.class) //
				.withElement(ProcessActivityDefinition.newInstance() //
						// not important
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(processInstanceActivities).read(anyString(), anyLong(), anyString());

		// when
		final SimpleResponse<ProcessActivityDefinition> response = cxfActivities.read("foo", "bar", 123L);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(processInstanceActivities).read("bar", 123L, "foo");
		verifyNoMoreInteractions(processInstanceActivities);
	}

	@Test
	public void readAllDelegatedCorrectly() throws Exception {
		// given
		final ListResponse<ProcessActivity> expectedResponse = ListResponse.newInstance(ProcessActivity.class) //
				.withElement(ProcessActivity.newInstance() //
						// not important
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(processInstanceActivities).read(anyString(), anyLong());

		// when
		final ListResponse<ProcessActivity> response = cxfActivities.read("foo", 123L);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(processInstanceActivities).read("foo", 123L);
		verifyNoMoreInteractions(processInstanceActivities);
	}

}
