package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static support.ServerResource.randomPort;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.common.collect.ChainablePutMap;
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
	public void instancesReadUsingGet() throws Exception {
		// given
		final String firstName = "foo";
		final String secondName = "bar";
		final Map<String, String> firstValues = ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz");
		final Map<String, String> secondValues = ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("bar", "baz");
		final ListResponse<ProcessInstance> sentResponse = ListResponse.newInstance(ProcessInstance.class) //
				.withElements(asList( //
						ProcessInstance.newInstance() //
								.withName(firstName) //
								.withValues(firstValues) //
								.build(), //
						ProcessInstance.newInstance() //
								.withName(secondName) //
								.withValues(secondValues) //
								.build() //
						)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2L) //
						.build()) //
				.build();
		@SuppressWarnings("unchecked")
		final ListResponse<Map<String, String>> expectedResponse = ListResponse.<Map<String, String>> newInstance() //
				.withElements(Arrays.<Map<String, String>> asList( //
						ChainablePutMap.of(new HashMap<String, String>()) //
								.chainablePut(UNDERSCORED_NAME, firstName) //
								.chainablePutAll(firstValues), //
						ChainablePutMap.of(new HashMap<String, String>()) //
								.chainablePut(UNDERSCORED_NAME, secondName) //
								.chainablePutAll(secondValues) //
						)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2L) //
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).readAll(anyString(), anyInt(), anyInt());

		// when
		final GetMethod get = new GetMethod(server.resource("processes/foo/instances/"));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
