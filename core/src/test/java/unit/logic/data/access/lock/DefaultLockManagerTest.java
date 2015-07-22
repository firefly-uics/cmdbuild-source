package unit.logic.data.access.lock;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Suppliers.ofInstance;
import static org.cmdbuild.logic.data.access.lock.Lockables.card;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cmdbuild.exception.ConsistencyException;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager;
import org.cmdbuild.logic.data.access.lock.LockManager;
import org.cmdbuild.logic.data.access.lock.Lockable;
import org.cmdbuild.logic.data.access.lock.LockableStore;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Supplier;

public class DefaultLockManagerTest {

	private static Supplier<String> usernameSupplier = ofInstance("test");

	private LockableStore<DefaultLockManager.Metadata> store;
	private LockManager manager;

	@Before
	public void setUp() throws Exception {
		store = mock(LockableStore.class);
		manager = new DefaultLockManager(usernameSupplier, store);
	}

	@Test
	public void checkedWhenNotLocked() throws Exception {
		// given
		final Lockable lockable = card(1L);
		doReturn(absent()) //
				.when(store).get(lockable);

		// when
		manager.checkNotLocked(lockable);
		verify(store).get(lockable);
	}

	@Test(expected = ConsistencyException.class)
	public void checkedAfterLocked() throws Exception {
		// given
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.checkNotLocked(lockable);
	}

	@Test
	public void checkedAfterLockedAndUnlocked() throws Exception {
		// given
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.unlock(lockable);
		manager.checkNotLocked(lockable);
	}

	@Test
	public void checkedWithUserWhenNotLocked() throws Exception {
		// given
		final Lockable lockable = card(1L);

		// when
		manager.checkLockedbyUser(lockable, "foo");
	}

	@Test(expected = ConsistencyException.class)
	public void checkedWithUserAfterLockedFromDifferentUser() throws Exception {
		// given
		final Supplier<String> usernameSupplier = mock(Supplier.class);
		when(usernameSupplier.get()) //
				.thenReturn("foo", "bar");
		manager = new DefaultLockManager( usernameSupplier, store);
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.checkLockedbyUser(lockable, "bar");
	}

	@Test
	public void lockedTwiceBySameUser() throws Exception {
		// given
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.lock(lockable);
	}

	@Test(expected = ConsistencyException.class)
	public void lockedTwiceByDifferenUser() throws Exception {
		// given
		final Supplier<String> usernameSupplier = mock(Supplier.class);
		when(usernameSupplier.get()) //
				.thenReturn("foo", "bar");
		manager = new DefaultLockManager( usernameSupplier, store);
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.lock(lockable);
	}

	@Test
	public void unlockedWhenNotLocked() throws Exception {
		// given
		final Lockable lockable = card(1L);

		// when
		manager.unlock(lockable);
	}

	@Test(expected = ConsistencyException.class)
	public void lockedAndTheUnlockedFromDifferentUsers() throws Exception {
		// given
		final Supplier<String> usernameSupplier = mock(Supplier.class);
		when(usernameSupplier.get()) //
				.thenReturn("foo", "bar");
		manager = new DefaultLockManager(usernameSupplier, store);
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.unlock(lockable);
	}

}
