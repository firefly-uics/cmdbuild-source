package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Models.newLookupDetail;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.LookupTypeValues;
import org.cmdbuild.service.rest.model.LookupDetail;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class LookupTypeValuesTest {

	private LookupTypeValues service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
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
		final ResponseMultiple<LookupDetail> expectedResponse = newResponseMultiple(LookupDetail.class) //
				.withElements(asList( //
						newLookupDetail() //
								.withId(34L) //
								.withCode("foo") //
								.build(), //
						newLookupDetail() //
								.withId(56L) //
								.withCode("bar") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readAll(anyString(), anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("lookup_types/12/values/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).readAll(eq("12"), eq(false), eq((Integer) null), eq((Integer) null));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getLookup() throws Exception {
		// given
		final ResponseSingle<LookupDetail> expectedResponse = newResponseSingle(LookupDetail.class) //
				.withElement(newLookupDetail() //
						.withType("12") //
						.withId(34L) //
						.withCode("code") //
						.withDescription("description") //
						.withNumber(56L) //
						.withParentType("parent_type") //
						.withParentId(78L) //
						.build()) //
				.build();
		when(service.read(anyString(), anyLong())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("lookup_types/12/values/34/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("12"), eq(34L));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
