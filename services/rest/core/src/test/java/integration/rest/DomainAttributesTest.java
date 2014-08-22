package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.DomainAttributes;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class DomainAttributesTest {

	private static DomainAttributes service;

	@ClassRule
	public static ServerResource server = ServerResource.newInstance() //
			.withServiceClass(DomainAttributes.class) //
			.withService(service = mock(DomainAttributes.class)) //
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
	public void getDomainAttributes() throws Exception {
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
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readAll(eq("foo"), anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("domains/foo/attributes/"));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
