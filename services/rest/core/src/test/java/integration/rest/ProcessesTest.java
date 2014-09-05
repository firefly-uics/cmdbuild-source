package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Processes;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.FullProcessDetail;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleProcessDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;
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
	public void getClasses() throws Exception {
		// given
		final ListResponse<SimpleProcessDetail> expectedResponse = ListResponse.<SimpleProcessDetail> newInstance() //
				.withElements(asList( //
						SimpleProcessDetail.newInstance() //
								.withName("foo") //
								.build(), //
						SimpleProcessDetail.newInstance() //
								.withName("bar") //
								.build())) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readAll(anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("processes/"));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getClassDetail() throws Exception {
		// given
		final SimpleResponse<FullProcessDetail> expectedResponse = SimpleResponse.<FullProcessDetail> newInstance() //
				.withElement(FullProcessDetail.newInstance() //
						.withName("foo") //
						.build()) //
				.build();
		when(service.read(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("processes/foo/"));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
