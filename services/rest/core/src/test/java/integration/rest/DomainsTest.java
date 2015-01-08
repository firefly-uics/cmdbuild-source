package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.START;
import static org.cmdbuild.service.rest.model.Models.newDomainWithBasicDetails;
import static org.cmdbuild.service.rest.model.Models.newDomainWithFullDetails;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
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
import org.cmdbuild.service.rest.Domains;
import org.cmdbuild.service.rest.model.DomainWithBasicDetails;
import org.cmdbuild.service.rest.model.DomainWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class DomainsTest {

	private Domains service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Domains.class) //
			.withService(service = mock(Domains.class)) //
			.withPort(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
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
		when(service.readAll(anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("domains/")) //
				.setParameter(ACTIVE, "true") //
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
	public void domainRead() throws Exception {
		// given
		final ResponseSingle<DomainWithFullDetails> expectedResponse = newResponseSingle(DomainWithFullDetails.class) //
				.withElement(newDomainWithFullDetails() //
						.withName("foo") //
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