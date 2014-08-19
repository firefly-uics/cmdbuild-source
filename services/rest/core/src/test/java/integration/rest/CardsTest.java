package integration.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.cmdbuild.service.rest.dto.CardListResponse;
import org.cmdbuild.service.rest.dto.CardResponse;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.NewCardResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import support.ForwardingProxy;
import support.JsonSupport;
import support.ServerResource;

public class CardsTest {

	private final ForwardingProxy<Cards> forwardingProxy = ForwardingProxy.of(Cards.class);
	private Cards service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Cards.class) //
			.withService(forwardingProxy.get()) //
			.withPort(8080) //
			.build();

	@Rule
	public JsonSupport json = new JsonSupport();

	private HttpClient httpclient;

	@Before
	public void mockService() throws Exception {
		service = mock(Cards.class);
		forwardingProxy.set(service);
	}

	@Before
	public void createHttpClient() throws Exception {
		httpclient = new HttpClient();
	}

	@Test
	public void getCards() throws Exception {
		// given
		final Map<String, Object> first = ChainablePutMap.of(new HashMap<String, Object>()) //
				.chainablePut("foo", "foo") //
				.chainablePut("bar", "bar");
		final Map<String, Object> second = ChainablePutMap.of(new HashMap<String, Object>()) //
				.chainablePut("bar", "baz");
		final CardListResponse expectedResponse = CardListResponse.newInstance() //
				.withElements(asList(first, second)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(2) //
						.build()) //
				.build();
		when(service.readAll("foo", null, null, null)) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/classes/foo/cards");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void createCard() throws Exception {
		// given
		final ArgumentCaptor<MultivaluedMap> multivaluedMapCaptor = ArgumentCaptor.forClass(MultivaluedMap.class);
		final NewCardResponse expectedResponse = NewCardResponse.newInstance() //
				.withElement(123L) //
				.build();
		when(service.create(eq("foo"), multivaluedMapCaptor.capture())) //
				.thenReturn(expectedResponse);

		// when
		final PostMethod post = new PostMethod("http://localhost:8080/classes/foo/cards/");
		post.addParameter("foo", "bar");
		post.addParameter("bar", "baz");
		post.addParameter("baz", "foo");
		final int result = httpclient.executeMethod(post);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(post.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
		final MultivaluedMap captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst("foo"), equalTo((Object) "bar"));
		assertThat(captured.getFirst("bar"), equalTo((Object) "baz"));
		assertThat(captured.getFirst("baz"), equalTo((Object) "foo"));
	}

	@Test
	public void readCard() throws Exception {
		// given
		final Map<String, Object> values = ChainablePutMap.of(new HashMap<String, Object>()) //
				.chainablePut("foo", "foo") //
				.chainablePut("bar", "bar") //
				.chainablePut("bar", "baz");
		final CardResponse expectedResponse = CardResponse.newInstance() //
				.withElement(values) //
				.build();
		when(service.read("foo", 123L)) //
				.thenReturn(expectedResponse);

		// when
		final GetMethod get = new GetMethod("http://localhost:8080/classes/foo/cards/123/");
		final int result = httpclient.executeMethod(get);

		// then
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void updateCard() throws Exception {
		// when
		final PutMethod put = new PutMethod("http://localhost:8080/classes/foo/cards/123/");
		final int result = httpclient.executeMethod(put);

		// then
		assertThat(result, equalTo(204));
	}

	@Test
	public void deleteCard() throws Exception {
		// when
		final DeleteMethod delete = new DeleteMethod("http://localhost:8080/classes/foo/cards/123/");
		final int result = httpclient.executeMethod(delete);

		// then
		assertThat(result, equalTo(204));
	}

}
