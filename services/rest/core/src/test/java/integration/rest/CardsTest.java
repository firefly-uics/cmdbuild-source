package integration.rest;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static support.ServerResource.randomPort;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.Cards;
import org.cmdbuild.service.rest.dto.Card;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import support.JsonSupport;
import support.ServerResource;

public class CardsTest {

	private Cards service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Cards.class) //
			.withService(service = mock(Cards.class)) //
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
	public void cardsRead() throws Exception {
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
								.chainablePut(UNDERSCORED_TYPE, type) //
								.chainablePut(UNDERSCORED_ID, firstId) //
								.chainablePutAll(firstValues), //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_TYPE, type) //
								.chainablePut(UNDERSCORED_ID, secondId) //
								.chainablePutAll(secondValues) //
						)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2L) //
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), isNull(String.class), anyInt(), anyInt());

		// when
		final GetMethod get = new GetMethod(server.resource("classes/foo/cards"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("foo"), isNull(String.class), anyInt(), anyInt());
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void cardCreated() throws Exception {
		// given
		final SimpleResponse<Long> expectedResponse = SimpleResponse.<Long> newInstance() //
				.withElement(123L) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), any(Card.class));

		// when
		final PostMethod post = new PostMethod(server.resource("classes/foo/cards/"));
		post.setRequestEntity(new StringRequestEntity( //
				"{\"_id\" : 123, \"_type\" : \"foo\", \"bar\" : \"BAR\", \"baz\" : \"BAZ\"}", //
				APPLICATION_JSON, //
				UTF_8) //
		);
		final int result = httpclient.executeMethod(post);

		// then
		final ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
		verify(service).create(eq("foo"), cardCaptor.capture());
		assertThat(result, equalTo(200));
		assertThat(json.from(post.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
		final Card captured = cardCaptor.getValue();
		assertThat(captured.getType(), equalTo("foo"));
		assertThat(captured.getId(), equalTo(123L));
		final Map<String, Object> values = captured.getValues();
		assertThat(values, hasEntry("bar", (Object) "BAR"));
		assertThat(values, hasEntry("baz", (Object) "BAZ"));
	}

	@Test
	public void cardRead() throws Exception {
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
						.chainablePut(UNDERSCORED_TYPE, type) //
						.chainablePut(UNDERSCORED_ID, firstId) //
						.chainablePutAll(firstValues) //
				) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyLong());

		// when
		final GetMethod get = new GetMethod(server.resource("classes/foo/cards/123/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("foo"), eq(123L));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void cardUpdated() throws Exception {
		// when
		final PutMethod put = new PutMethod(server.resource("classes/foo/cards/123/"));
		put.setRequestEntity(new StringRequestEntity( //
				"{\"_id\" : 123, \"_type\" : \"foo\", \"bar\" : \"BAR\", \"baz\" : \"BAZ\"}", //
				APPLICATION_JSON, //
				UTF_8) //
		);
		final int result = httpclient.executeMethod(put);

		// then
		final ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
		verify(service).update(eq("foo"), eq(123L), cardCaptor.capture());
		assertThat(result, equalTo(204));
		final Card captured = cardCaptor.getValue();
		assertThat(captured.getType(), equalTo("foo"));
		assertThat(captured.getId(), equalTo(123L));
		final Map<String, Object> values = captured.getValues();
		assertThat(values, hasEntry("bar", (Object) "BAR"));
		assertThat(values, hasEntry("baz", (Object) "BAZ"));
	}

	@Test
	public void cardDeleted() throws Exception {
		// when
		final DeleteMethod delete = new DeleteMethod(server.resource("classes/foo/cards/123/"));
		final int result = httpclient.executeMethod(delete);

		// then
		verify(service).delete(eq("foo"), eq(123L));
		assertThat(result, equalTo(204));
	}

}
