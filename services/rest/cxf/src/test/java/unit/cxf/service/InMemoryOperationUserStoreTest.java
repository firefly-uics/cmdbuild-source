package unit.cxf.service;

import static org.cmdbuild.service.rest.model.Builders.newSession;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.service.rest.cxf.service.InMemoryOperationUserStore;
import org.cmdbuild.service.rest.model.Session;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class InMemoryOperationUserStoreTest {

	private InMemoryOperationUserStore store;

	@Before
	public void setUp() throws Exception {
		store = new InMemoryOperationUserStore();
	}

	@Test(expected = NullPointerException.class)
	public void puttingValueWithNullKeyThrowsException() throws Exception {
		// given
		final OperationUser value = operationUser();

		// when
		store.put(null, value);
	}

	@Test(expected = NullPointerException.class)
	public void puttingNullValueThrowsException() throws Exception {
		// given
		final Session key = session("id");

		// when
		store.put(key, null);
	}

	@Test(expected = NullPointerException.class)
	public void gettingNullKeyThrowsException() throws Exception {
		// when
		store.get(null);
	}

	@Test
	public void missingDataReturnsAbsent() throws Exception {
		// given
		final Session missing = session("missing");

		// when
		final Optional<OperationUser> shouldBeAbsent = store.get(missing);

		// then
		assertThat(shouldBeAbsent.isPresent(), equalTo(false));
	}

	@Test
	public void putAndRead() throws Exception {
		// given
		final Session key = session("id");
		final OperationUser value = operationUser();

		// when
		store.put(key, value);
		final Optional<OperationUser> stored = store.get(key);

		// then
		assertThat(stored.isPresent(), equalTo(true));
		assertThat(stored.get(), equalTo(value));
	}

	@Test(expected = NullPointerException.class)
	public void removingNullKeyThrowsException() throws Exception {
		// when
		store.remove(null);
	}

	@Test
	public void removingMissingIdDoesNothing() throws Exception {
		// given
		final Session missing = session("missing");

		// when
		store.remove(missing);
	}

	@Test
	public void putRemoveAndRead() throws Exception {
		// given
		final Session key = session("id");
		final OperationUser value = operationUser();

		// when
		store.put(key, value);
		store.remove(key);
		final Optional<OperationUser> stored = store.get(key);

		// then
		assertThat(stored.isPresent(), equalTo(false));
	}

	private static Session session(final String id) {
		return newSession().withId(id).build();
	}

	private static OperationUser operationUser() {
		return new OperationUser(mock(AuthenticatedUser.class), mock(PrivilegeContext.class), mock(CMGroup.class));
	}

}
