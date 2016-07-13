package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;
import static org.cmdbuild.service.rest.v2.model.Models.newDomainWithBasicDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newDomainWithFullDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
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
import org.cmdbuild.service.rest.v2.Domains;
import org.cmdbuild.service.rest.v2.model.DomainWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.DomainWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class DomainsTest {

	@ClassRule
	public static ServerResource<Domains> server = ServerResource.newInstance(Domains.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private Domains service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(Domains.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void domainsRead() throws Exception {
		// given
		final ResponseMultiple<DomainWithBasicDetails> expectedResponse = newResponseMultiple(
				DomainWithBasicDetails.class) //
						.withElements(asList( //
								newDomainWithBasicDetails() //
										.withName("foo") //
										.build(), //
								newDomainWithBasicDetails() //
										.withName("bar") //
										.build())) //
						.withMetadata(newMetadata() //
								.withTotal(2L) //
								.build()) //
						.build();
		when(service.readAll(anyString(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("domains/")) //
				.setParameter(FILTER, "filter") //
				.setParameter(LIMIT, "123") //
				.setParameter(START, "456") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).readAll(eq("filter"), eq(123), eq(456));
	}

	@Test
	public void domainRead() throws Exception {
		// given
		final ResponseSingle<DomainWithFullDetails> expectedResponse = newResponseSingle(DomainWithFullDetails.class) //
				.withElement(newDomainWithFullDetails() //
						.withName("foo") //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		when(service.read(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("domains/123/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read("123");
	}

}
