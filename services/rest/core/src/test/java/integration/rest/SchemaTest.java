package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Schema;
import org.cmdbuild.service.rest.dto.ClassDetail;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import support.ForwardingSchema;
import support.JsonSupport;
import support.ServerResource;

public class SchemaTest {

	private final ForwardingSchema forwardingSchema = new ForwardingSchema();
	private Schema service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Schema.class) //
			.withService(forwardingSchema) //
			.withPort(8080) //
			.build();

	@Rule
	public JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void mockService() throws Exception {
		service = mock(Schema.class);
		forwardingSchema.setInner(service);
	}

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void getClasses() throws Exception {
		// given
		final ClassDetailResponse expectedResponse = ClassDetailResponse.newInstance() //
				.withDetails(asList( //
						ClassDetail.newInstance() //
								.withName("foo") //
								.build(), //
						ClassDetail.newInstance() //
								.withName("bar") //
								.build())) //
				.withTotal(2) //
				.build();
		when(service.getClasses()) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/schema/classes/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
