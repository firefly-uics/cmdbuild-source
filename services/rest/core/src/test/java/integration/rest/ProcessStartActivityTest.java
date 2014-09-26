package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Builders.newAttributeStatus;
import static org.cmdbuild.service.rest.model.Builders.newProcessActivityWithFullDetails;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.ProcessStartActivity;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class ProcessStartActivityTest {

	private ProcessStartActivity service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(ProcessStartActivity.class) //
			.withService(service = mock(ProcessStartActivity.class)) //
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
	public void read() throws Exception {
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
				.when(service).read(anyString());

		// when
		final GetMethod get = new GetMethod(server.resource("processes/foo/start_activity/"));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
