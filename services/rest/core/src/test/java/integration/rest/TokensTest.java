package integration.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.cmdbuild.service.rest.model.Builders.newCredentials;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static support.ServerResource.randomPort;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.cmdbuild.service.rest.Tokens;
import org.cmdbuild.service.rest.model.Credentials;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import support.JsonSupport;
import support.ServerResource;

public class TokensTest {

	private Tokens service;

	@Rule
	public ServerResource server = ServerResource.newInstance() //
			.withServiceClass(Tokens.class) //
			.withService(service = mock(Tokens.class)) //
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
	public void tokenCreated() throws Exception {
		// given
		final ResponseSingle<String> sentResponse = newResponseSingle(String.class) //
				.withElement("token") //
				.build();
		doReturn(sentResponse) //
				.when(service).create(any(Credentials.class));

		// when
		final PostMethod post = new PostMethod(server.resource("tokens/"));
		post.setRequestEntity(new StringRequestEntity( //
				"{\"username\" : \"foo\", \"password\" : \"bar\"}", //
				APPLICATION_JSON, //
				UTF_8) //
		);
		final int result = httpclient.executeMethod(post);

		// then
		final ArgumentCaptor<Credentials> captor = ArgumentCaptor.forClass(Credentials.class);
		verify(service).create(captor.capture());

		final Credentials captured = captor.getValue();
		assertThat(captured.getUsername(), equalTo("foo"));
		assertThat(captured.getPassword(), equalTo("bar"));

		assertThat(result, equalTo(200));
		assertThat(json.from(post.getResponseBodyAsString()), equalTo(json.from(sentResponse)));
	}

	@Test
	public void cardReaded() throws Exception {
		// given
		final ResponseSingle<Credentials> sentResponse = newResponseSingle(Credentials.class) //
				.withElement(newCredentials() //
						.withToken("t") //
						.withUsername("u") //
						.withPassword("p") //
						.withGroup("g") //
						.build() //
				) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString());

		// when
		final GetMethod get = new GetMethod(server.resource("tokens/foo/"));
		final int result = httpclient.executeMethod(get);

		// then
		verify(service).read(eq("foo"));
		assertThat(result, equalTo(200));
		assertThat(json.from(get.getResponseBodyAsString()), equalTo(json.from(sentResponse)));
	}

	@Test
	public void tokenUpdated() throws Exception {
		// when
		final PutMethod put = new PutMethod(server.resource("tokens/foo/"));
		put.setRequestEntity(new StringRequestEntity( //
				"{\"token\" : null, \"username\" : \"bar\", \"password\" : null, \"group\" : \"baz\"}", //
				APPLICATION_JSON, //
				UTF_8) //
		);
		final int result = httpclient.executeMethod(put);

		// then
		final ArgumentCaptor<Credentials> captor = ArgumentCaptor.forClass(Credentials.class);
		verify(service).update(eq("foo"), captor.capture());

		final Credentials captured = captor.getValue();
		assertThat(captured.getUsername(), equalTo("bar"));
		assertThat(captured.getGroup(), equalTo("baz"));

		assertThat(result, equalTo(204));
	}

	@Test
	public void tokenDeleted() throws Exception {
		// when
		final DeleteMethod delete = new DeleteMethod(server.resource("tokens/foo/"));
		final int result = httpclient.executeMethod(delete);

		// then
		verify(service).delete(eq("foo"));
		assertThat(result, equalTo(204));
	}

}
