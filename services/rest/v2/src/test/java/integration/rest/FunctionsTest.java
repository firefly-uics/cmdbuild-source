package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;
import static org.cmdbuild.service.rest.v2.model.Models.newAttribute;
import static org.cmdbuild.service.rest.v2.model.Models.newFunctionWithBasicDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newFunctionWithFullDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v2.Functions;
import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.FunctionWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.FunctionWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class FunctionsTest {

	private Functions service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Functions.class) //
			.withService(service = mock(Functions.class)) //
			.withPortRange(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void functionsRetrieved() throws Exception {
		// given
		final ResponseMultiple<FunctionWithBasicDetails> expectedResponse = newResponseMultiple(
				FunctionWithBasicDetails.class) //
				.withElements(asList( //
						newFunctionWithBasicDetails() //
								.withId(1L) //
								.withName("foo") //
								.withDescription("Foo") //
								.build(), //
						newFunctionWithBasicDetails() //
								.withId(2L) //
								.withName("bar") //
								.withDescription("Bar") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readAll(anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("functions/")) //
				.setParameter(LIMIT, "123") //
				.setParameter(START, "456") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).readAll(eq(123), eq(456));
	}

	@Test
	public void functionDetailsRetrieved() throws Exception {
		// given
		final ResponseSingle<FunctionWithFullDetails> expectedResponse = newResponseSingle(
				FunctionWithFullDetails.class) //
				.withElement(newFunctionWithFullDetails() //
						.withId(1L) //
						.withName("foo") //
						.withDescription("Foo") //
						.build()) //
				.build();
		when(service.read(anyLong())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("functions/1/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq(1L));
	}

	@Test
	public void functionParametersRetrieved() throws Exception {
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
		when(service.readInputParameters(anyLong(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("functions/1/parameters/")) //
				.setParameter(LIMIT, "456") //
				.setParameter(START, "789") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).readInputParameters(eq(1L), eq(456), eq(789));
	}

	@Test
	public void functionAttributesRetrieved() throws Exception {
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
		when(service.readOutputParameters(anyLong(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("functions/1/attributes/")) //
				.setParameter(LIMIT, "456") //
				.setParameter(START, "789") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).readOutputParameters(eq(1L), eq(456), eq(789));
	}

}
