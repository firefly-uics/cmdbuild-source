package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.dto.Builders.newAttributeStatus;
import static org.cmdbuild.service.rest.dto.Builders.newMetadata;
import static org.cmdbuild.service.rest.dto.Builders.newProcessActivityWithBasicDetails;
import static org.cmdbuild.service.rest.dto.Builders.newProcessActivityWithFullDetails;
import static org.cmdbuild.service.rest.dto.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.dto.Builders.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.ProcessInstanceActivities;
import org.cmdbuild.service.rest.dto.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.dto.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.dto.ResponseMultiple;
import org.cmdbuild.service.rest.dto.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class ProcessInstanceActivitiesTest {

	private ProcessInstanceActivities service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(ProcessInstanceActivities.class) //
			.withService(service = mock(ProcessInstanceActivities.class)) //
			.withPort(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void instancesRead() throws Exception {
		// given
		final ResponseMultiple<ProcessActivityWithBasicDetails> sentResponse = newResponseMultiple(
				ProcessActivityWithBasicDetails.class) //
				.withElements(asList( //
						newProcessActivityWithBasicDetails() //
								.withId(123L) //
								.withWritableStatus(true) //
								.build(), //
						newProcessActivityWithBasicDetails() //
								.withId(456L) //
								.withWritableStatus(false) //
								.build() //
						)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		final ResponseMultiple<ProcessActivityWithBasicDetails> expectedResponse = sentResponse;
		doReturn(sentResponse) //
				.when(service).read("foo", 123L);

		// when
		final GetMethod get = new GetMethod(server.resource("processes/foo/instances/123/activities"));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void instanceRead() throws Exception {
		// given
		final ResponseSingle<ProcessActivityWithFullDetails> sentResponse = newResponseSingle(
				ProcessActivityWithFullDetails.class) //
				.withElement(newProcessActivityWithFullDetails() //
						.withId(123L) //
						.withDescription("description") //
						.withInstructions("instructions") //
						.withAttributes(asList( //
								newAttributeStatus() //
										.withId(456L) //
										.withWritable(true) //
										.withMandatory(false) //
										.build(), //
								newAttributeStatus() //
										.withId(789L) //
										.withMandatory(true) //
										.build() //
								)) //
						.build()) //
				.build();
		final ResponseSingle<ProcessActivityWithFullDetails> expectedResponse = sentResponse;
		doReturn(sentResponse) //
				.when(service).read("foo", 123L, "bar");

		// when
		final GetMethod get = new GetMethod(server.resource("processes/foo/instances/123/activities/bar"));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
