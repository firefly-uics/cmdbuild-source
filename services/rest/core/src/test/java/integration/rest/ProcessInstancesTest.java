package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.ProcessInstances;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessInstance;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class ProcessInstancesTest {

	private static ProcessInstances service;

	@ClassRule
	public static ServerResource server = ServerResource.newInstance() //
			.withServiceClass(ProcessInstances.class) //
			.withService(service = mock(ProcessInstances.class)) //
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
	public void getInstances() throws Exception {
		// given
		final ListResponse<ProcessInstance> expectedResponse = ListResponse.<ProcessInstance> newInstance() //
				.withElements(asList( //
						ProcessInstance.newInstance() //
								.withName("bar") //
								.build(), //
						ProcessInstance.newInstance() //
								.withName("baz") //
								.build() //
						)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2L) //
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(service).readAll(anyString(), anyInt(), anyInt());

		// when
		final GetMethod get = new GetMethod(server.resource("processes/foo/instances/"));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
