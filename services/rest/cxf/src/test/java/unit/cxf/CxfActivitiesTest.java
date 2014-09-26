package unit.cxf;

import static org.cmdbuild.service.rest.model.Builders.newProcessActivityWithBasicDetails;
import static org.cmdbuild.service.rest.model.Builders.newProcessActivityWithFullDetails;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;
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
import org.cmdbuild.service.rest.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
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
		final ResponseSingle<ProcessActivityWithFullDetails> expectedResponse = newResponseSingle(
				ProcessActivityWithFullDetails.class) //
				.withElement(newProcessActivityWithFullDetails() //
						// not important
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(processInstanceActivities).read(anyString(), anyLong(), anyString());

		// when
		final ResponseSingle<ProcessActivityWithFullDetails> response = cxfActivities.read("foo", "bar", 123L);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(processInstanceActivities).read("bar", 123L, "foo");
		verifyNoMoreInteractions(processInstanceActivities);
	}

	@Test
	public void readAllDelegatedCorrectly() throws Exception {
		// given
		final ResponseMultiple<ProcessActivityWithBasicDetails> expectedResponse = newResponseMultiple(
				ProcessActivityWithBasicDetails.class) //
				.withElement(newProcessActivityWithBasicDetails() //
						// not important
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(processInstanceActivities).read(anyString(), anyLong());

		// when
		final ResponseMultiple<ProcessActivityWithBasicDetails> response = cxfActivities.read("foo", 123L);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(processInstanceActivities).read("foo", 123L);
		verifyNoMoreInteractions(processInstanceActivities);
	}

}
