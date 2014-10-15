package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.START;
import static org.cmdbuild.service.rest.model.Builders.newAttribute;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.HttpClientUtils.all;
import static support.HttpClientUtils.param;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.DomainAttributes;
import org.cmdbuild.service.rest.model.Attribute;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class DomainAttributesTest {

	private DomainAttributes service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
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
		final ResponseMultiple<Attribute> expectedResponse = newResponseMultiple(Attribute.class) //
				.withElements(asList( //
						newAttribute() //
								.withName("bar") //
								.build(), //
						newAttribute() //
								.withName("baz") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readAll(anyString(), anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("domains/123/attributes/"));
		get.setQueryString(all( //
				param(ACTIVE, "true"), //
				param(LIMIT, "456"), //
				param(START, "789") //
		));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).readAll(eq("123"), eq(true), eq(456), eq(789));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
