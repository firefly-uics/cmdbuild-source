package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.cmdbuild.service.rest.Classes;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.CardListResponse;
import org.cmdbuild.service.rest.dto.CardResponse;
import org.cmdbuild.service.rest.dto.ClassDetail;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import support.ForwardingProxy;
import support.JsonSupport;
import support.ServerResource;

public class ClassesTest {

	private final ForwardingProxy<Classes> forwardingProxy = ForwardingProxy.of(Classes.class);
	private Classes service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Classes.class) //
			.withService(forwardingProxy.get()) //
			.withPort(8080) //
			.build();

	@Rule
	public JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void mockService() throws Exception {
		service = mock(Classes.class);
		forwardingProxy.set(service);
	}

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void getClasses() throws Exception {
		// given
		final ClassDetailResponse expectedResponse = ClassDetailResponse.newInstance() //
				.withElements(asList( //
						ClassDetail.newInstance() //
								.withName("foo") //
								.build(), //
						ClassDetail.newInstance() //
								.withName("bar") //
								.build())) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2) //
						.build()) //
				.build();
		when(service.getClasses(anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/classes/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getClassAttributes() throws Exception {
		// given
		final AttributeDetailResponse expectedResponse = AttributeDetailResponse.newInstance() //
				.withElements(asList( //
						AttributeDetail.newInstance() //
								.withName("bar") //
								.build(), //
						AttributeDetail.newInstance() //
								.withName("baz") //
								.build())) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2) //
						.build()) //
				.build();
		when(service.getAttributes(eq("foo"), anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/classes/foo/attributes/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getCards() throws Exception {
		// given
		final Map<String, Object> first = new HashMap<String, Object>() {
			{
				put("foo", "foo");
				put("bar", "bar");
			}
		};
		final Map<String, Object> second = new HashMap<String, Object>() {
			{
				put("bar", "baz");
			}
		};
		final CardListResponse expectedResponse = CardListResponse.newInstance() //
				.withElements(asList(first, second)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2) //
						.build()) //
				.build();
		when(service.getCards("foo", null, null)) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/classes/foo/cards");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void getCard() throws Exception {
		// given
		final Map<String, Object> values = new HashMap<String, Object>() {
			{
				put("foo", "foo");
				put("bar", "bar");
				put("bar", "baz");
			}
		};
		final CardResponse expectedResponse = CardResponse.newInstance() //
				.withElement(values) //
				.build();
		when(service.getCard("foo", 123L)) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/classes/foo/cards/123/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}
}
