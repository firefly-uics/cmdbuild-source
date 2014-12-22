package integration.rest;

import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.cmdbuild.service.rest.Impersonate;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class ImpersonateTest {

	private Impersonate service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Impersonate.class) //
			.withService(service = mock(Impersonate.class)) //
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
	public void start() throws Exception {
		// when
		final PutMethod put = new PutMethod(server.resource("sessions/foo/impersonate/bar/"));
		final int result = httpclient.executeMethod(put);

		// then
		verify(service).start(eq("foo"), eq("bar"));
		assertThat(result, equalTo(204));
	}

	@Test
	public void stop() throws Exception {
		// when
		final DeleteMethod delete = new DeleteMethod(server.resource("sessions/foo/impersonate/"));
		final int result = httpclient.executeMethod(delete);

		// then
		verify(service).stop(eq("foo"));
		assertThat(result, equalTo(204));
	}

}
