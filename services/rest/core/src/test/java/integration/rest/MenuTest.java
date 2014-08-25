package integration.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Menu;
import org.cmdbuild.service.rest.dto.MenuDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class MenuTest {

	private static Menu service;

	@ClassRule
	public static ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Menu.class) //
			.withService(service = mock(Menu.class)) //
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
	public void getMenu() throws Exception {
		// given
		final SimpleResponse<MenuDetail> expectedResponse = SimpleResponse.<MenuDetail> newInstance() //
				.withElement(MenuDetail.newInstance() //
						.withType("root") //
						.build()) //
				.build();
		when(service.read()) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("menu/"));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}