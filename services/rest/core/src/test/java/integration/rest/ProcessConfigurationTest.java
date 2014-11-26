package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newProcessStatus;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.ProcessesConfiguration;
import org.cmdbuild.service.rest.model.ProcessStatus;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class ProcessConfigurationTest {

	private ProcessesConfiguration service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(ProcessesConfiguration.class) //
			.withService(service = mock(ProcessesConfiguration.class)) //
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
	public void readStatuses() throws Exception {
		// given
		final ResponseMultiple<ProcessStatus> expectedResponse = newResponseMultiple(ProcessStatus.class) //
				.withElements(asList( //
						newProcessStatus() //
								.withId(123L) //
								.withDescription("bar") //
								.build(), //
						newProcessStatus() //
								.withId(456L) //
								.withDescription("baz") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readStatuses()) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("configuration/processes/statuses/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).readStatuses();
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
