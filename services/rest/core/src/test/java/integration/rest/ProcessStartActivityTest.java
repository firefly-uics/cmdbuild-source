package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.ProcessStartActivity;
import org.cmdbuild.service.rest.dto.ProcessActivityDefinition;
import org.cmdbuild.service.rest.dto.ProcessActivityDefinition.Attribute;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class ProcessStartActivityTest {

	private static ProcessStartActivity service;

	@ClassRule
	public static ServerResource server = ServerResource.newInstance() //
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
		final SimpleResponse<ProcessActivityDefinition> sentResponse = SimpleResponse
				.newInstance(ProcessActivityDefinition.class) //
				.withElement(ProcessActivityDefinition.newInstance() //
						.withId("id") //
						.withDescription("description") //
						.withInstructions("instructions") //
						.withAttributes(asList( //
								Attribute.newInstance() //
										.withId("foo") //
										.withWritable(true) //
										.withMandatory(false) //
										.build(), //
								Attribute.newInstance() //
										.withId("bar") //
										.withMandatory(true) //
										.build() //
								)) //
						.build()) //
				.build();
		final SimpleResponse<ProcessActivityDefinition> expectedResponse = sentResponse;
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
