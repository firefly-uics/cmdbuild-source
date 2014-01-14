package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Schema;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.ClassDetail;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.LookupDetailResponse;
import org.cmdbuild.service.rest.dto.LookupTypeDetail;
import org.cmdbuild.service.rest.dto.LookupTypeDetailResponse;
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
		when(service.getClasses(anyBoolean())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/schema/classes/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getAttributes() throws Exception {
		// given
		final AttributeDetailResponse expectedResponse = AttributeDetailResponse.newInstance() //
				.withDetails(asList( //
						AttributeDetail.newInstance() //
								.withName("bar") //
								.build(), //
						AttributeDetail.newInstance() //
								.withName("baz") //
								.build())) //
				.withTotal(2) //
				.build();
		when(service.getAttributes(eq("foo"), anyBoolean())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/schema/classes/foo/attributes/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getLookupTypes() throws Exception {
		// given
		final LookupTypeDetailResponse expectedResponse = LookupTypeDetailResponse.newInstance() //
				.withDetails(asList( //
						LookupTypeDetail.newInstance() //
								.withName("foo") //
								.build(), //
						LookupTypeDetail.newInstance() //
								.withName("bar") //
								.build())) //
				.withTotal(2) //
				.build();
		when(service.getLookupTypes()) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/schema/lookup/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getLookups() throws Exception {
		// given
		final LookupDetailResponse expectedResponse = LookupDetailResponse.newInstance() //
				.withDetails(asList( //
						LookupDetail.newInstance() //
								.withId(123L) //
								.withCode("foo") //
								.build(), //
						LookupDetail.newInstance() //
								.withId(456L) //
								.withCode("bar") //
								.build())) //
				.withTotal(2) //
				.build();
		when(service.getLookups("foo", false)) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/schema/lookup/foo/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
