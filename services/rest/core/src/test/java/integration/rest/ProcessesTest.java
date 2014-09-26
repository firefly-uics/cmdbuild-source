package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.START;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newProcessWithBasicDetails;
import static org.cmdbuild.service.rest.model.Builders.newProcessWithFullDetails;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.HttpClientUtils.all;
import static support.HttpClientUtils.param;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Processes;
import org.cmdbuild.service.rest.model.ProcessWithBasicDetails;
import org.cmdbuild.service.rest.model.ProcessWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class ProcessesTest {

	private Processes service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Processes.class) //
			.withService(service = mock(Processes.class)) //
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
	public void getProcesses() throws Exception {
		// given
		final ResponseMultiple<ProcessWithBasicDetails> expectedResponse = newResponseMultiple(
				ProcessWithBasicDetails.class) //
				.withElements(asList( //
						newProcessWithBasicDetails() //
								.withName("foo") //
								.build(), //
						newProcessWithBasicDetails() //
								.withName("bar") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readAll(anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("processes/"));
		get.setQueryString(all( //
				param(ACTIVE, "true"), //
				param(LIMIT, "123"), //
				param(START, "456") //
		));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).readAll(eq(true), eq(123), eq(456));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getProcessDetail() throws Exception {
		// given
		final ResponseSingle<ProcessWithFullDetails> expectedResponse = newResponseSingle(ProcessWithFullDetails.class) //
				.withElement(newProcessWithFullDetails() //
						.withName("foo") //
						.build()) //
				.build();
		when(service.read(anyLong())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("processes/123/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(123L);
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
