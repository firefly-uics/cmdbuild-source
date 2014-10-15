package unit.cxf.service;

import static org.cmdbuild.service.rest.model.Builders.newCredentials;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.cmdbuild.service.rest.cxf.service.InMemoryTokenStore;
import org.cmdbuild.service.rest.model.Credentials;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class InMemoryTokenStoreTest {

	private InMemoryTokenStore store;

	@Before
	public void setUp() throws Exception {
		store = new InMemoryTokenStore();
	}

	@Test(expected = NullPointerException.class)
	public void puttingNullTokenThrowsException() throws Exception {
		// given
		final Credentials credentials = newCredentials() //
				.withToken("token") //
				.withUsername("username") //
				.withPassword("password") //
				.withGroup("password") //
				.build();

		// when
		store.put(null, credentials);
	}

	@Test(expected = NullPointerException.class)
	public void puttingNullCredentialsThrowsException() throws Exception {
		// when
		store.put("token", null);
	}

	@Test(expected = NullPointerException.class)
	public void gettingNullTokenThrowsException() throws Exception {
		// when
		store.get(null);
	}

	@Test
	public void missingDataReturnsAbsent() throws Exception {
		// when
		final Optional<Credentials> shouldBeAbsent = store.get("missing");

		// then
		assertThat(shouldBeAbsent.isPresent(), equalTo(false));
	}

	@Test
	public void putAndRead() throws Exception {
		// given
		final Credentials credentials = newCredentials() //
				.withToken("token") //
				.withUsername("username") //
				.withPassword("password") //
				.withGroup("password") //
				.build();

		// when
		store.put("token", credentials);
		final Optional<Credentials> stored = store.get("token");

		// then
		assertThat(stored.isPresent(), equalTo(true));
		assertThat(stored.get(), equalTo(credentials));
	}

	@Test(expected = NullPointerException.class)
	public void removingNullTokenThrowsException() throws Exception {
		// when
		store.remove(null);
	}

	@Test
	public void removingMissingTokenDoesNothing() throws Exception {
		// when
		store.remove("missing");
	}

	@Test
	public void putRemoveAndRead() throws Exception {
		// given
		final Credentials credentials = newCredentials() //
				.withToken("token") //
				.withUsername("username") //
				.withPassword("password") //
				.withGroup("password") //
				.build();

		// when
		store.put("token", credentials);
		store.remove("token");
		final Optional<Credentials> stored = store.get("token");

		// then
		assertThat(stored.isPresent(), equalTo(false));
	}

}
