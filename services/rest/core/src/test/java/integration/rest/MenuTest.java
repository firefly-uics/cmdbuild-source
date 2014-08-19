package integration.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Menu;
import org.cmdbuild.service.rest.dto.MenuDetail;
import org.cmdbuild.service.rest.dto.MenuDetailResponse;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import support.ForwardingProxy;
import support.JsonSupport;
import support.ServerResource;

public class MenuTest {

	private final ForwardingProxy<Menu> forwardingProxy = ForwardingProxy.of(Menu.class);
	private Menu service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Menu.class) //
			.withService(forwardingProxy.get()) //
			.withPort(8080) //
			.build();

	@Rule
	public JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void mockService() throws Exception {
		service = mock(Menu.class);
		forwardingProxy.set(service);
	}

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void getMenu() throws Exception {
		// given
		final SimpleResponse<MenuDetail> expectedResponse = MenuDetailResponse.newInstance() //
				.withElement(MenuDetail.newInstance() //
						.withType("root") //
						.build()) //
				.build();
		when(service.read()) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/menu/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
