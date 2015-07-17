package unit.logic.data.access.lock;

import static com.google.common.base.Suppliers.ofInstance;
import static org.cmdbuild.logic.data.access.lock.Lockables.card;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.exception.ConsistencyException;
import org.cmdbuild.logic.data.access.lock.InMemoryLockManager;
import org.cmdbuild.logic.data.access.lock.InMemoryLockManager.Configuration;
import org.cmdbuild.logic.data.access.lock.LockManager;
import org.cmdbuild.logic.data.access.lock.LockManager.Lockable;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Supplier;

public class InMemoryLockManagerTest {

	private static Configuration configuration = new Configuration() {

		@Override
		public boolean isUsernameVisible() {
			return true;
		}

		@Override
		public long getExpirationTimeInMilliseconds() {
			return 10000;
		}

	};
	private static Supplier<String> usernameSupplier = ofInstance("test");

	private LockManager manager;

	@Before
	public void setUp() throws Exception {
		manager = new InMemoryLockManager(configuration, usernameSupplier);
	}

	@Test
	public void checkedWhenNotLocked() {
		// given
		final Lockable lockable = card(1L);

		// when
		manager.checkNotLocked(lockable);
	}

	@Test(expected = ConsistencyException.class)
	public void checkedAfterLocked() {
		// given
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.checkNotLocked(lockable);
	}

	@Test
	public void checkedAfterLockedAndUnlocked() {
		// given
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.unlock(lockable);
		manager.checkNotLocked(lockable);
	}

	@Test
	public void checkedWithUserWhenNotLocked() {
		// given
		final Lockable lockable = card(1L);

		// when
		manager.checkLockedbyUser(lockable, "foo");
	}

	@Test(expected = ConsistencyException.class)
	public void checkedWithUserAfterLockedFromDifferentUser() {
		// given
		final Supplier<String> usernameSupplier = mock(Supplier.class);
		when(usernameSupplier.get()) //
				.thenReturn("foo", "bar");
		manager = new InMemoryLockManager(configuration, usernameSupplier);
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.checkLockedbyUser(lockable, "bar");
	}

	@Test
	public void lockedTwiceBySameUser() {
		// given
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.lock(lockable);
	}

	@Test(expected = ConsistencyException.class)
	public void lockedTwiceByDifferenUser() {
		// given
		final Supplier<String> usernameSupplier = mock(Supplier.class);
		when(usernameSupplier.get()) //
				.thenReturn("foo", "bar");
		manager = new InMemoryLockManager(configuration, usernameSupplier);
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.lock(lockable);
	}

	@Test
	public void unlockedWhenNotLocked() {
		// given
		final Lockable lockable = card(1L);

		// when
		manager.unlock(lockable);
	}

	@Test(expected = ConsistencyException.class)
	public void lockedAndTheUnlockedFromDifferentUsers() {
		// given
		final Supplier<String> usernameSupplier = mock(Supplier.class);
		when(usernameSupplier.get()) //
				.thenReturn("foo", "bar");
		manager = new InMemoryLockManager(configuration, usernameSupplier);
		final Lockable lockable = card(1L);

		// when
		manager.lock(lockable);
		manager.unlock(lockable);
	}

}
