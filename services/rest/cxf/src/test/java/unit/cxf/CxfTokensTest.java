package unit.cxf;

import static org.cmdbuild.service.rest.model.Builders.newCredentials;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.service.rest.cxf.CxfTokens;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.cxf.service.TokenGenerator;
import org.cmdbuild.service.rest.cxf.service.TokenStore;
import org.cmdbuild.service.rest.model.Credentials;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.base.Optional;

public class CxfTokensTest {

	private ErrorHandler errorHandler;
	private TokenGenerator tokenGenerator;
	private TokenStore tokenStore;

	private CxfTokens cxfTokens;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		tokenGenerator = mock(TokenGenerator.class);
		tokenStore = mock(TokenStore.class);
		cxfTokens = new CxfTokens(errorHandler, tokenGenerator, tokenStore);
	}

	@Test(expected = WebApplicationException.class)
	public void create_missingUsernameThrowsException() throws Exception {
		// given
		final Credentials credentials = newCredentials() //
				.withPassword("foo") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingUsername();

		// when
		cxfTokens.create(credentials);
	}

	@Test(expected = WebApplicationException.class)
	public void create_blankUsernameThrowsException() throws Exception {
		// given
		final Credentials credentials = newCredentials() //
				.withUsername(" \t") //
				.withPassword("foo") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingUsername();

		// when
		cxfTokens.create(credentials);
	}

	@Test(expected = WebApplicationException.class)
	public void create_missingPasswordThrowsException() throws Exception {
		// given
		final Credentials credentials = newCredentials() //
				.withUsername("foo") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingPassword();

		// when
		cxfTokens.create(credentials);
	}

	@Test(expected = WebApplicationException.class)
	public void create_blankPasswordThrowsException() throws Exception {
		// given
		final Credentials credentials = newCredentials() //
				.withUsername("foo") //
				.withPassword(" \t") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingPassword();

		// when
		cxfTokens.create(credentials);
	}

	@Test
	public void create_tokenSuccessfullyGeneratedStoredAndReturned() throws Exception {
		// given
		final Credentials credentials = newCredentials() //
				.withUsername("username") //
				.withPassword("password") //
				.build();
		doReturn("token") //
				.when(tokenGenerator).generate(anyString());

		// when
		final ResponseSingle<String> response = cxfTokens.create(credentials);

		// then
		final InOrder inOrder = inOrder(tokenGenerator, tokenStore);
		inOrder.verify(tokenGenerator).generate(eq(credentials.getUsername()));
		inOrder.verify(tokenStore).put(eq("token"), eq(newCredentials(credentials) //
				.withToken("token") //
				.build()));
		inOrder.verifyNoMoreInteractions();
		assertThat(response.getElement(), equalTo("token"));
	}

	@Test(expected = WebApplicationException.class)
	public void read_missingTokenThrowsException() throws Exception {
		// given
		doReturn(Optional.absent()) //
				.when(tokenStore).get(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).tokenNotFound(anyString());

		// when
		cxfTokens.read("token");

		// then
		verify(tokenStore).get(eq("token"));
	}

	@Test
	public void read_passwordNotReturned() throws Exception {
		// given
		final Credentials credentials = newCredentials() //
				.withToken("token") //
				.withUsername("username") //
				.withPassword("password") //
				.withGroup("group") //
				.build();
		doReturn(Optional.of(credentials)) //
				.when(tokenStore).get(anyString());

		// when
		final ResponseSingle<Credentials> response = cxfTokens.read("token");

		// then
		verify(tokenStore).get(eq("token"));
		assertThat(response.getElement(), equalTo(newCredentials(credentials) //
				.withPassword(null) //
				.build()));
	}

	@Test(expected = WebApplicationException.class)
	public void update_missingTokenThrowsException() throws Exception {
		// given
		final Credentials credentials = newCredentials() //
				.withToken("token") //
				.withUsername("username") //
				.withPassword("password") //
				.withGroup("group") //
				.build();
		doReturn(Optional.absent()) //
				.when(tokenStore).get(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).tokenNotFound(anyString());

		// when
		cxfTokens.update("token", credentials);

		// then
		verify(tokenStore).get(eq("token"));
	}

	@Test
	public void update_onlyGroupUpdated() throws Exception {
		// given
		final Credentials oldCredentials = newCredentials() //
				.withToken("old token") //
				.withUsername("old username") //
				.withPassword("old password") //
				.withGroup("old group") //
				.build();
		final Credentials newCredentials = newCredentials() //
				.withToken("new token") //
				.withUsername("new username") //
				.withPassword("new password") //
				.withGroup("new group") //
				.build();
		doReturn(Optional.of(oldCredentials)) //
				.when(tokenStore).get(anyString());

		// when
		cxfTokens.update("token", newCredentials);

		// then
		verify(tokenStore).get(eq("token"));
		verify(tokenStore).put(eq("token"), eq(newCredentials(oldCredentials) //
				.withGroup(newCredentials.getGroup()) //
				.build()));
	}

	@Test(expected = WebApplicationException.class)
	public void delete_missingTokenThrowsException() throws Exception {
		// given
		doReturn(Optional.absent()) //
				.when(tokenStore).get(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).tokenNotFound(anyString());

		// when
		cxfTokens.delete("token");

		// then
		verify(tokenStore).get(eq("token"));
	}

	@Test
	public void update_tokenRemovedFromStore() throws Exception {
		// given
		final Credentials credentials = newCredentials() //
				.withToken("old token") //
				.withUsername("old username") //
				.withPassword("old password") //
				.withGroup("old group") //
				.build();
		doReturn(Optional.of(credentials)) //
				.when(tokenStore).get(anyString());

		// when
		cxfTokens.delete("token");

		// then
		verify(tokenStore).get(eq("token"));
		verify(tokenStore).remove(eq("token"));
	}

}
