package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Lookups;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.LookupDetailResponse;
import org.cmdbuild.service.rest.dto.LookupResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import support.ForwardingProxy;
import support.JsonSupport;
import support.ServerResource;

public class LookupsTest {

	private final ForwardingProxy<Lookups> forwardingProxy = ForwardingProxy.of(Lookups.class);
	private Lookups service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Lookups.class) //
			.withService(forwardingProxy.get()) //
			.withPort(8080) //
			.build();

	@Rule
	public JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void mockService() throws Exception {
		service = mock(Lookups.class);
		forwardingProxy.set(service);
	}

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void getLookups() throws Exception {
		// given
		final LookupDetailResponse expectedResponse = LookupDetailResponse.newInstance() //
				.withElements(asList( //
						LookupDetail.newInstance() //
								.withId(123L) //
								.withCode("foo") //
								.build(), //
						LookupDetail.newInstance() //
								.withId(456L) //
								.withCode("bar") //
								.build())) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2) //
						.build()) //
				.build();
		when(service.readAll(eq("foo"), eq(false), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/lookuptypes/foo/values/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getLookup() throws Exception {
		// given
		final LookupResponse expectedResponse = LookupResponse.newInstance() //
				.withElement(LookupDetail.newInstance() //
						.withType("type") //
						.withId(123L) //
						.withCode("code") //
						.withDescription("description") //
						.withNumber(42) //
						.withParentType("parent_type") //
						.withParentId(456L) //
						.build()) //
				.build();
		when(service.read(eq("foo"), eq(123L))) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/lookuptypes/foo/values/123/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
