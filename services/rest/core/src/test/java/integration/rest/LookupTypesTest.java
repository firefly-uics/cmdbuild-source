package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.dto.Builders.newLookupTypeDetail;
import static org.cmdbuild.service.rest.dto.Builders.newMetadata;
import static org.cmdbuild.service.rest.dto.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.dto.Builders.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.LookupTypes;
import org.cmdbuild.service.rest.dto.LookupTypeDetail;
import org.cmdbuild.service.rest.dto.ResponseMultiple;
import org.cmdbuild.service.rest.dto.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class LookupTypesTest {

	private LookupTypes service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(LookupTypes.class) //
			.withService(service = mock(LookupTypes.class)) //
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
	public void getLookupTypes() throws Exception {
		// given
		final ResponseMultiple<LookupTypeDetail> expectedResponse = newResponseMultiple(LookupTypeDetail.class) //
				.withElements(asList( //
						newLookupTypeDetail() //
								.withName("foo") //
								.build(), //
						newLookupTypeDetail() //
								.withName("bar") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readAll(anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("lookup_types/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).readAll(null, null);
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getLookupType() throws Exception {
		// given
		final ResponseSingle<LookupTypeDetail> expectedResponse = newResponseSingle(LookupTypeDetail.class) //
				.withElement(newLookupTypeDetail() //
						.withName("foo") //
						.build()) //
				.build();
		when(service.read(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("lookup_types/foo/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read("foo");
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
