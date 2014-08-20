package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Attributes;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import support.ForwardingProxy;
import support.JsonSupport;
import support.ServerResource;

public class AttributesTest {

	private final ForwardingProxy<Attributes> forwardingProxy = ForwardingProxy.of(Attributes.class);
	private Attributes service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Attributes.class) //
			.withService(forwardingProxy.get()) //
			.withPort(8080) //
			.build();

	@Rule
	public JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void mockService() throws Exception {
		service = mock(Attributes.class);
		forwardingProxy.set(service);
	}

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void getAttributesForName() throws Exception {
		// given
		final ListResponse<AttributeDetail> expectedResponse = ListResponse.<AttributeDetail> newInstance() //
				.withElements(asList( //
						AttributeDetail.newInstance() //
								.withName("bar") //
								.build(), //
						AttributeDetail.newInstance() //
								.withName("baz") //
								.build())) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2) //
						.build()) //
				.build();
		when(service.readAll(eq("anything"), eq("foo"), anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/attributes/anything/foo/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
