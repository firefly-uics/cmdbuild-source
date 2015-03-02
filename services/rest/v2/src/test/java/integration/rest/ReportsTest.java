package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;
import static org.cmdbuild.service.rest.v2.model.Models.newAttribute;
import static org.cmdbuild.service.rest.v2.model.Models.newLongIdAndDescription;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
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
import org.cmdbuild.service.rest.v2.Reports;
import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.LongIdAndDescription;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class ReportsTest {

	private Reports service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Reports.class) //
			.withService(service = mock(Reports.class)) //
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
	public void readAll() throws Exception {
		// given
		final ResponseMultiple<LongIdAndDescription> sentResponse = newResponseMultiple(LongIdAndDescription.class) //
				.withElements(asList( //
						newLongIdAndDescription() //
								.setId(1L) //
								.setDescription("foo") //
								.build(), //
						newLongIdAndDescription() //
								.setId(2L) //
								.setDescription("bar") //
								.build() //
						)) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()).build();
		doReturn(sentResponse) //
				.when(service).readAll(anyInt(), anyInt());

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("reports/")) //
				.setParameter(LIMIT, "12") //
				.setParameter(START, "34") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(sentResponse)));

		verify(service).readAll(eq(12), eq(34));
	}

	@Test
	public void readAllAttributes() throws Exception {
		// given
		final ResponseMultiple<Attribute> sentResponse = newResponseMultiple(Attribute.class) //
				.withElements(asList( //
						newAttribute() //
								.withName("bar") //
								.build(), //
						newAttribute() //
								.withName("baz") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		when(service.readAllAttributes(anyLong(), anyInt(), anyInt())) //
				.thenReturn(sentResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("reports/123/attributes/")) //
				.setParameter(LIMIT, "456") //
				.setParameter(START, "789") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(sentResponse)));

		verify(service).readAllAttributes(eq(123L), eq(456), eq(789));
	}

}
