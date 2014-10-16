package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newProcessActivityWithBasicDetails;
import static org.cmdbuild.service.rest.model.Builders.newProcessActivityWithFullDetails;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.ProcessStartActivities;
import org.cmdbuild.service.rest.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class ProcessStartActivitiesTest {

	private ProcessStartActivities service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(ProcessStartActivities.class) //
			.withService(service = mock(ProcessStartActivities.class)) //
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
	public void allStartActivitiesRead() throws Exception {
		// given
		final ProcessActivityWithBasicDetails firstActivity = newProcessActivityWithBasicDetails() //
				.withId("foo") //
				.withDescription("foo foo foo") //
				.build();
		final ProcessActivityWithBasicDetails secondActivity = newProcessActivityWithBasicDetails() //
				.withId("bar") //
				.withDescription("bar bar bar") //
				.build();
		final ResponseMultiple<ProcessActivityWithBasicDetails> sentResponse = newResponseMultiple(
				ProcessActivityWithBasicDetails.class) //
				.withElements(asList(firstActivity, secondActivity)) //
				.withMetadata(newMetadata() //
						.withTotal(3L) //
						.build()) //
				.build();
		final ResponseMultiple<ProcessActivityWithBasicDetails> expectedResponse = sentResponse;
		doReturn(sentResponse) //
				.when(service).read(anyString());

		// when
		final GetMethod get = new GetMethod(server.resource("processes/baz/start_activities/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("baz"));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void startActivityDetailRead() throws Exception {
		// given
		final ProcessActivityWithFullDetails firstActivity = newProcessActivityWithFullDetails() //
				.withId("foo") //
				.withDescription("foo foo foo") //
				.withInstructions("blah blah blah") //
				.build();
		final ResponseSingle<ProcessActivityWithFullDetails> sentResponse = newResponseSingle(
				ProcessActivityWithFullDetails.class) //
				.withElement(firstActivity) //
				.build();
		final ResponseSingle<ProcessActivityWithFullDetails> expectedResponse = sentResponse;
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyString());

		// when
		final GetMethod get = new GetMethod(server.resource("processes/bar/start_activities/baz/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("bar"), eq("baz"));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
