package integration.rest;

import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v1.model.Models.newMenu;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v1.Menu;
import org.cmdbuild.service.rest.v1.model.MenuDetail;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class MenuTest {

	private Menu service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Menu.class) //
			.withService(service = mock(Menu.class)) //
			.withPort(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void getMenu() throws Exception {
		// given
		final ResponseSingle<MenuDetail> expectedResponse = newResponseSingle(MenuDetail.class) //
				.withElement(newMenu() //
						.withMenuType("root") //
						.build()) //
				.build();
		when(service.read()) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("menu/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read();
	}

}
