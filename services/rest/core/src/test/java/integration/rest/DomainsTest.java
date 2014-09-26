package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Builders.newDomainWithBasicDetails;
import static org.cmdbuild.service.rest.model.Builders.newDomainWithFullDetails;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Domains;
import org.cmdbuild.service.rest.model.DomainWithBasicDetails;
import org.cmdbuild.service.rest.model.DomainWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

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
		httpclient = new HttpClient();
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
		final GetMethod get = new GetMethod(server.resource("domains/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).readAll(isNull(Integer.class), isNull(Integer.class));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void domainRead() throws Exception {
		// given
		final ResponseSingle<DomainWithFullDetails> expectedResponse = newResponseSingle(DomainWithFullDetails.class) //
				.withElement(newDomainWithFullDetails() //
						.withName("foo") //
						.build()) //
				.build();
		when(service.read(eq("foo"))) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("domains/foo/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("foo"));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
