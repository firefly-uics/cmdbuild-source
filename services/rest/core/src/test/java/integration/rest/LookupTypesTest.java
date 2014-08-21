package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.LookupTypes;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.LookupTypeDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import support.ForwardingProxy;
import support.JsonSupport;
import support.ServerResource;

public class LookupTypesTest {

	private final ForwardingProxy<LookupTypes> forwardingProxy = ForwardingProxy.of(LookupTypes.class);
	private LookupTypes service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(LookupTypes.class) //
			.withService(forwardingProxy.get()) //
			.withPort(8080) //
			.build();

	@Rule
	public JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void mockService() throws Exception {
		service = mock(LookupTypes.class);
		forwardingProxy.set(service);
	}

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void getLookupTypes() throws Exception {
		// given
		final ListResponse<LookupTypeDetail> expectedResponse = ListResponse.<LookupTypeDetail> newInstance() //
				.withElements(asList( //
						LookupTypeDetail.newInstance() //
								.withName("foo") //
								.build(), //
						LookupTypeDetail.newInstance() //
								.withName("bar") //
								.build())) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readAll(anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/lookuptypes/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getLookupType() throws Exception {
		// given
		final SimpleResponse<LookupTypeDetail> expectedResponse = SimpleResponse.<LookupTypeDetail> newInstance() //
				.withElement(LookupTypeDetail.newInstance() //
						.withName("foo") //
						.build()) //
				.build();
		when(service.read(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/lookuptypes/foo/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
