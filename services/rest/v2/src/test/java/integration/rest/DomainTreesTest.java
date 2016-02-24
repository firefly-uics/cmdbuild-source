package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newNode;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
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
import org.cmdbuild.service.rest.v2.DomainTrees;
import org.cmdbuild.service.rest.v2.model.Node;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class DomainTreesTest {

	private DomainTrees service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(DomainTrees.class) //
			.withService(service = mock(DomainTrees.class)) //
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
	public void allElementsRead() throws Exception {
		// given
		final ResponseMultiple<String> expectedResponse = newResponseMultiple(String.class) //
				.withElements(asList("foo", "bar")) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		when(service.readAll(anyString(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("domainTrees/")) //
				.setParameter(FILTER, "the filter") //
				.setParameter(LIMIT, "123") //
				.setParameter(START, "456") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).readAll(eq("the filter"), eq(123), eq(456));
	}

	@Test
	public void elementRead() throws Exception {
		// given
		final ResponseMultiple<Node> expectedResponse = newResponseMultiple(Node.class) //
				.withElements(asList( //
						newNode() //
								.withId(123L) //
								.build(), //
						newNode() //
								.withId(456L) //
								.build() //
		)) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		when(service.read(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("domainTrees/foo/")) //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("foo"));
	}

}
