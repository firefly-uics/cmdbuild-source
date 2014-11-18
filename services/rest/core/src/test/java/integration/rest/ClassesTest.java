package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.constants.Serialization.START;
import static org.cmdbuild.service.rest.model.Models.newClassWithBasicDetails;
import static org.cmdbuild.service.rest.model.Models.newClassWithFullDetails;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static support.HttpClientUtils.all;
import static support.HttpClientUtils.param;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Classes;
import org.cmdbuild.service.rest.model.ClassWithBasicDetails;
import org.cmdbuild.service.rest.model.ClassWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import support.JsonSupport;
import support.ServerResource;

public class ClassesTest {

	private Classes service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Classes.class) //
			.withService(service = mock(Classes.class)) //
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
	public void getClasses() throws Exception {
		// given
		final ResponseMultiple<ClassWithBasicDetails> expectedResponse = newResponseMultiple(
				ClassWithBasicDetails.class) //
				.withElements(asList( //
						newClassWithBasicDetails() //
								.withName("foo") //
								.build(), //
						newClassWithBasicDetails() //
								.withName("bar") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readAll(anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("classes/"));
		get.setQueryString(all( //
				param(ACTIVE, "true"), //
				param(LIMIT, "123"), //
				param(START, "456") //
		));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).readAll(eq(true), eq(123), eq(456));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getClassDetail() throws Exception {
		// given
		final ResponseSingle<ClassWithFullDetails> expectedResponse = newResponseSingle(ClassWithFullDetails.class) //
				.withElement(newClassWithFullDetails() //
						.withName("foo") //
						.build()) //
				.build();
		when(service.read(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod(server.resource("classes/123/"));
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

}
