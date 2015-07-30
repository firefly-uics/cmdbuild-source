package unit.logic.data.access.lock;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Suppliers.ofInstance;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cmdbuild.logic.data.access.lock.DefaultLockManager;
import org.cmdbuild.logic.data.access.lock.InMemoryLockableStore;
import org.cmdbuild.logic.data.access.lock.LockManager;
import org.cmdbuild.logic.data.access.lock.LockManager.ExpectedLocked;
import org.cmdbuild.logic.data.access.lock.LockManager.LockedByAnotherUser;
import org.cmdbuild.logic.data.access.lock.Lockable;
import org.cmdbuild.logic.data.access.lock.LockableStore;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

public class DefaultLockManagerTest {

	private static class LockableWithParent implements Lockable {

		private final Lockable parent;

		/**
		 * Use factory method.
		 */
		private LockableWithParent(final Lockable parent) {
			this.parent = parent;
		}

		@Override
		public Optional<Lockable> parent() {
			return of(parent);
		}

	}

	private static final Lockable lockable(final Lockable parent) {
		return new LockableWithParent(parent);
	}

	private static class LockableWithNoParent implements Lockable {

		/**
		 * Use factory method.
		 */
		private LockableWithNoParent() {
		}

		@Override
		public Optional<Lockable> parent() {
			return absent();
		}

	}

	private static final Lockable lockable() {
		return new LockableWithNoParent();
	}

	private static Supplier<String> usernameSupplier = ofInstance("test");

	private LockableStore<DefaultLockManager.Lock> store;
	private LockManager manager;

	@Before
	public void setUp() throws Exception {
		store = new InMemoryLockableStore<DefaultLockManager.Lock>();
		manager = new DefaultLockManager(store, usernameSupplier);
	}

	@Test
	public void checkedWhenNotLocked() throws Exception {
		// given
		final Lockable lockable = lockable();
		store = mock(LockableStore.class);
		manager = new DefaultLockManager(store, usernameSupplier);
		doReturn(absent()) //
				.when(store).get(eq(lockable));
		doReturn(emptyList()) //
				.when(store).stored();

		// when
		manager.checkNotLocked(lockable);

		// then
		verify(store).get(lockable);
		verify(store).stored();
	}

	@Test(expected = LockedByAnotherUser.class)
	public void checkedAfterLocked() throws Exception {
		// given
		final Lockable lockable = lockable();

		// when
		manager.lock(lockable);
		manager.checkNotLocked(lockable);
	}

	@Test(expected = LockedByAnotherUser.class)
	public void parentCheckedAfterChildHasBeenLocked() throws Exception {
		// given
		final Lockable parent = lockable();
		final Lockable child = lockable(parent);

		// when
		manager.lock(child);
		manager.checkNotLocked(parent);
	}

	@Test
	public void checkedAfterLockedAndUnlocked() throws Exception {
		// given
		final Lockable lockable = lockable();

		// when
		manager.lock(lockable);
		manager.unlock(lockable);
		manager.checkNotLocked(lockable);
	}

	@Test(expected = ExpectedLocked.class)
	public void checkedWithUserWhenNotLocked() throws Exception {
		// given
		final Lockable lockable = lockable();

		// when
		manager.checkLockedByUser(lockable, "foo");
	}

	@Test(expected = LockedByAnotherUser.class)
	public void checkedWithUserAfterLockedFromDifferentUser() throws Exception {
		// given
		final Supplier<String> usernameSupplier = mock(Supplier.class);
		when(usernameSupplier.get()) //
				.thenReturn("foo", "bar");
		manager = new DefaultLockManager(store, usernameSupplier);
		final Lockable lockable = lockable();

		// when
		manager.lock(lockable);
		manager.checkLockedByUser(lockable, "bar");
	}

	@Test
	public void lockedTwiceBySameUser() throws Exception {
		// given
		final Lockable lockable = lockable();

		// when
		manager.lock(lockable);
		manager.lock(lockable);
	}

	@Test(expected = LockedByAnotherUser.class)
	public void lockedTwiceByDifferenUser() throws Exception {
		// given
		final Supplier<String> usernameSupplier = mock(Supplier.class);
		when(usernameSupplier.get()) //
				.thenReturn("foo", "bar");
		manager = new DefaultLockManager(store, usernameSupplier);
		final Lockable lockable = lockable();

		// when
		manager.lock(lockable);
		manager.lock(lockable);
	}

	@Test
	public void unlockedWhenNotLocked() throws Exception {
		// given
		final Lockable lockable = lockable();

		// when
		manager.unlock(lockable);
	}

	@Test(expected = LockedByAnotherUser.class)
	public void lockedAndTheUnlockedFromDifferentUsers() throws Exception {
		// given
		final Supplier<String> usernameSupplier = mock(Supplier.class);
		when(usernameSupplier.get()) //
				.thenReturn("foo", "bar");
		manager = new DefaultLockManager(store, usernameSupplier);
		final Lockable lockable = lockable();

		// when
		manager.lock(lockable);
		manager.unlock(lockable);
	}

	@Test(expected = LockedByAnotherUser.class)
	public void unlockingParent() throws Exception {
		// given
		final Lockable parent = lockable();
		final Lockable child = lockable(parent);

		// when
		manager.lock(child);
		manager.unlock(parent);
	}

}
