package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.LookupTypeValues;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class LookupTypeValuesTest {

	private static LookupTypeValues service;

	@ClassRule
	public static ServerResource server = ServerResource.newInstance() //
			.withServiceClass(LookupTypeValues.class) //
			.withService(service = mock(LookupTypeValues.class)) //
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
	public void getLookups() throws Exception {
		// given
		final ListResponse<LookupDetail> expectedResponse = ListResponse.<LookupDetail> newInstance() //
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
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readAll(anyString(), anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("lookup_types/foo/values/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).readAll("foo", false, null, null);
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getLookup() throws Exception {
		// given
		final SimpleResponse<LookupDetail> expectedResponse = SimpleResponse.<LookupDetail> newInstance() //
				.withElement(LookupDetail.newInstance() //
						.withType("type") //
						.withId(123L) //
						.withCode("code") //
						.withDescription("description") //
						.withNumber(42L) //
						.withParentType("parent_type") //
						.withParentId(456L) //
						.build()) //
				.build();
		when(service.read(anyString(), anyLong())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("lookup_types/foo/values/123/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read("foo", 123L);
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
