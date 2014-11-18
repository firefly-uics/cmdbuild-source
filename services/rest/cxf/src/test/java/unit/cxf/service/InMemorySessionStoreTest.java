package unit.cxf.service;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.model.Models.newSession;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.cmdbuild.service.rest.cxf.service.InMemorySessionStore;
import org.cmdbuild.service.rest.model.Session;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class InMemorySessionStoreTest {

	private InMemorySessionStore store;

	@Before
	public void setUp() throws Exception {
		store = new InMemorySessionStore();
	}

	@Test(expected = NullPointerException.class)
	public void puttingElementWithNullIdThrowsException() throws Exception {
		// given
		final Session element = newSession() //
				.withId(null) //
				.withUsername("username") //
				.withPassword("password") //
				.withRole("password") //
				.build();

		// when
		store.put(element);
	}

	@Test(expected = IllegalArgumentException.class)
	public void puttingElementWithEmptyIdThrowsException() throws Exception {
		// given
		final Session element = newSession() //
				.withId(EMPTY) //
				.withUsername("username") //
				.withPassword("password") //
				.withRole("password") //
				.build();

		// when
		store.put(element);
	}

	@Test(expected = IllegalArgumentException.class)
	public void puttingElementWithBlankIdThrowsException() throws Exception {
		// given
		final Session element = newSession() //
				.withId(" \t") //
				.withUsername("username") //
				.withPassword("password") //
				.withRole("password") //
				.build();

		// when
		store.put(element);
	}

	@Test(expected = NullPointerException.class)
	public void puttingNullElementThrowsException() throws Exception {
		// when
		store.put(null);
	}

	@Test(expected = NullPointerException.class)
	public void gettingNullIdThrowsException() throws Exception {
		// when
		store.get(null);
	}

	@Test
	public void missingDataReturnsAbsent() throws Exception {
		// when
		final Optional<Session> shouldBeAbsent = store.get("missing");

		// then
		assertThat(shouldBeAbsent.isPresent(), equalTo(false));
	}

	@Test
	public void putAndRead() throws Exception {
		// given
		final Session session = newSession() //
				.withId("id") //
				.withUsername("username") //
				.withPassword("password") //
				.withRole("password") //
				.build();

		// when
		store.put(session);
		final Optional<Session> stored = store.get("id");

		// then
		assertThat(stored.isPresent(), equalTo(true));
		assertThat(stored.get(), equalTo(session));
	}

	@Test(expected = NullPointerException.class)
	public void removingNullIdThrowsException() throws Exception {
		// when
		store.remove(null);
	}

	@Test
	public void removingMissingIdDoesNothing() throws Exception {
		// when
		store.remove("missing");
	}

	@Test
	public void putRemoveAndRead() throws Exception {
		// given
		final Session session = newSession() //
				.withId("id") //
				.withUsername("username") //
				.withPassword("password") //
				.withRole("password") //
				.build();

		// when
		store.put(session);
		store.remove("id");
		final Optional<Session> stored = store.get("id");

		// then
		assertThat(stored.isPresent(), equalTo(false));
	}

}
