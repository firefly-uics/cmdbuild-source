package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Models.newAttributeStatus;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newProcessActivityWithBasicDetails;
import static org.cmdbuild.service.rest.model.Models.newProcessActivityWithFullDetails;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.ProcessInstanceActivities;
import org.cmdbuild.service.rest.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

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
								.withId("123") //
								.withWritableStatus(true) //
								.build(), //
						newProcessActivityWithBasicDetails() //
								.withId("456") //
								.withWritableStatus(false) //
								.build() //
						)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		final ResponseMultiple<ProcessActivityWithBasicDetails> expectedResponse = sentResponse;
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyLong());

		// when
		final GetMethod get = new GetMethod(server.resource("processes/123/instances/456/activities"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("123"), eq(456L));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void instanceRead() throws Exception {
		// given
		final ResponseSingle<ProcessActivityWithFullDetails> sentResponse = newResponseSingle(
				ProcessActivityWithFullDetails.class) //
				.withElement(newProcessActivityWithFullDetails() //
						.withId("123") //
						.withDescription("description") //
						.withInstructions("instructions") //
						.withAttributes(asList( //
								newAttributeStatus() //
										.withId("456") //
										.withWritable(true) //
										.withMandatory(false) //
										.withIndex(0L) //
										.build(), //
								newAttributeStatus() //
										.withId("789") //
										.withMandatory(true) //
										.withIndex(1L) //
										.build() //
								)) //
						.build()) //
				.build();
		final ResponseSingle<ProcessActivityWithFullDetails> expectedResponse = sentResponse;
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyLong(), anyString());

		// when
		final GetMethod get = new GetMethod(server.resource("processes/123/instances/456/activities/789/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("123"), eq(456L), eq("789"));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
