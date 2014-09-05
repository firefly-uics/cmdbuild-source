package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.ProcessInstanceActivities;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessActivity;
import org.cmdbuild.service.rest.dto.ProcessActivityDefinition;
import org.cmdbuild.service.rest.dto.ProcessActivityDefinition.Attribute;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class ProcessInstanceActivitiesTest {

	private static ProcessInstanceActivities service;

	@ClassRule
	public static ServerResource server = ServerResource.newInstance() //
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
		final ListResponse<ProcessActivity> sentResponse = ListResponse.newInstance(ProcessActivity.class) //
				.withElements(asList( //
						ProcessActivity.newInstance() //
								.withId("first") //
								.withWritableStatus(true) //
								.build(), //
						ProcessActivity.newInstance() //
								.withId("second") //
								.withWritableStatus(false) //
								.build() //
						)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2L) //
						.build()) //
				.build();
		final ListResponse<ProcessActivity> expectedResponse = sentResponse;
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
				.when(service).read("foo", 123L, "bar");

		// when
		final GetMethod get = new GetMethod(server.resource("processes/foo/instances/123/activities/bar"));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
