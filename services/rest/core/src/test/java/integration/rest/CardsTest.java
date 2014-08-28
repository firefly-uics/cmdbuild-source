package integration.rest;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_CLASSNAME;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static support.HttpClientUtils.all;
import static support.HttpClientUtils.param;
import static support.ServerResource.randomPort;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.Cards;
import org.cmdbuild.service.rest.dto.Card;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

import support.JsonSupport;
import support.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class CardsTest {

	private static Cards service;

	@ClassRule
	public static ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Cards.class) //
			.withService(service = mock(Cards.class)) //
			.withPort(randomPort()) //
			.build();

	@ClassRule
	public static JsonSupport json = new JsonSupport();

	@Captor
	private ArgumentCaptor<MultivaluedMap<String, String>> multivaluedMapCaptor;

	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void cardsReadUsingGet() throws Exception {
		// given
		final String type = "foo";
		final Long firstId = 123L;
		final Long secondId = 456L;
		final Map<String, String> firstValues = ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz");
		final Map<String, String> secondValues = ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("bar", "baz");
		final ListResponse<Card> sentResponse = ListResponse.newInstance(Card.class) //
				.withElements(asList( //
						Card.newInstance() //
								.withType(type) //
								.withId(firstId) //
								.withValues(firstValues) //
								.build(), //
						Card.newInstance() //
								.withType(type) //
								.withId(secondId) //
								.withValues(secondValues) //
								.build() //
						)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2L) //
						.build()) //
				.build();
		@SuppressWarnings("unchecked")
		final ListResponse<Map<String, Object>> expectedResponse = ListResponse.<Map<String, Object>> newInstance() //
				.withElements(Arrays.<Map<String, Object>> asList( //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_CLASSNAME, type) //
								.chainablePut(UNDERSCORED_ID, firstId) //
								.chainablePutAll(firstValues), //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_CLASSNAME, type) //
								.chainablePut(UNDERSCORED_ID, secondId) //
								.chainablePutAll(secondValues) //
						)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2L) //
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).readAll(anyString(), isNull(String.class), anyInt(), anyInt());

		// when
		final GetMethod get = new GetMethod(server.resource("cards/"));
		get.setQueryString(all(param("_classname", "foo")));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).readAll(eq("foo"), isNull(String.class), anyInt(), anyInt());
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void cardCreatedUsingPost() throws Exception {
		// given
		final SimpleResponse<Long> expectedResponse = SimpleResponse.<Long> newInstance() //
				.withElement(123L) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(multivaluedMapCaptor.capture());

		// when
		final PostMethod post = new PostMethod(server.resource("cards/"));
		post.addParameter("_classname", "foo");
		post.addParameter("foo", "bar");
		post.addParameter("bar", "baz");
		post.addParameter("baz", "foo");
		final int result = httpclient.executeMethod(post);

		// then
		verify(service).create(multivaluedMapCaptor.capture());
		assertThat(result, equalTo(200));
		assertThat(json.from(post.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst("_classname"), equalTo((Object) "foo"));
		assertThat(captured.getFirst("foo"), equalTo((Object) "bar"));
		assertThat(captured.getFirst("bar"), equalTo((Object) "baz"));
		assertThat(captured.getFirst("baz"), equalTo((Object) "foo"));
	}

	@Test
	public void cardReadUsingGet() throws Exception {
		// given
		final String type = "foo";
		final Long firstId = 123L;
		final Map<String, String> firstValues = ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz") //
				.chainablePut("bar", "baz");
		final SimpleResponse<Card> sentResponse = SimpleResponse.newInstance(Card.class) //
				.withElement(Card.newInstance() //
						.withType(type) //
						.withId(firstId) //
						.withValues(firstValues) //
						.build() //
				) //
				.build();
		final SimpleResponse<Map<String, Object>> expectedResponse = SimpleResponse.<Map<String, Object>> newInstance() //
				.withElement(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut(UNDERSCORED_CLASSNAME, type) //
						.chainablePut(UNDERSCORED_ID, firstId) //
						.chainablePutAll(firstValues) //
				) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyLong());

		// when
		final GetMethod get = new GetMethod(server.resource("cards/123/"));
		get.setQueryString(all(param("_classname", "foo")));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("foo"), eq(123L));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void cardUpdatedUsingPut() throws Exception {
		// when
		final PutMethod put = new PutMethod(server.resource("cards/123/"));
		put.addRequestHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
		put.setQueryString(all( //
				param("_classname", "foo"), //
				param("foo", "bar"), //
				param("bar", "baz"), //
				param("baz", "foo") //
		));
		final int result = httpclient.executeMethod(put);

		// then
		verify(service).update(eq(123L), multivaluedMapCaptor.capture());
		assertThat(result, equalTo(204));
		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst("_classname"), equalTo((Object) "foo"));
		assertThat(captured.getFirst("foo"), equalTo((Object) "bar"));
		assertThat(captured.getFirst("bar"), equalTo((Object) "baz"));
		assertThat(captured.getFirst("baz"), equalTo((Object) "foo"));
	}

	@Test
	public void cardDeletedUsingDelete() throws Exception {
		// when
		final DeleteMethod delete = new DeleteMethod(server.resource("cards/123/"));
		delete.setQueryString(all(param("_classname", "foo")));
		final int result = httpclient.executeMethod(delete);

		// then
		verify(service).delete(eq("foo"), eq(123L));
		assertThat(result, equalTo(204));
	}

}
