package unit.logic.data.access.lock;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Lists.newArrayList;
import static org.cmdbuild.logic.data.access.lock.Lockables.card;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.cmdbuild.logic.data.access.lock.InMemoryLockableStore;
import org.cmdbuild.logic.data.access.lock.Lockable;
import org.cmdbuild.logic.data.access.lock.LockableStore;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class InMemoryLockableStoreTest {

	private static class DummyLock implements LockableStore.Lock {

	}

	private static final Optional<DummyLock> ABSENT = absent();

	private InMemoryLockableStore<DummyLock> store;

	@Before
	public void setUp() throws Exception {
		store = new InMemoryLockableStore<DummyLock>();
	}

	@Test
	public void elementNotStored() throws Exception {
		// given
		final Lockable element = card(1L);

		// then
		assertThat(store.isPresent(element), equalTo(false));
		assertThat(store.get(element), equalTo(ABSENT));
	}

	@Test
	public void elementStored() throws Exception {
		// given
		final Lockable element = card(1L);
		final DummyLock metadata = new DummyLock();

		// when
		store.add(element, metadata);

		// then
		assertThat(store.isPresent(element), equalTo(true));
		assertThat(store.get(element), equalTo(of(metadata)));
	}

	@Test
	public void multipleElementsStored() throws Exception {
		// given
		final Lockable first = card(1L);
		final DummyLock firstMetadata = new DummyLock();
		final Lockable second = card(2L);
		final DummyLock secondMetadata = new DummyLock();
		final Lockable third = card(3L);

		// when
		store.add(first, firstMetadata);
		store.add(second, secondMetadata);

		// then
		assertThat(store.isPresent(first), equalTo(true));
		assertThat(store.get(first), equalTo(of(firstMetadata)));
		assertThat(store.isPresent(second), equalTo(true));
		assertThat(store.get(second), equalTo(of(secondMetadata)));
		assertThat(store.isPresent(third), equalTo(false));
		assertThat(store.get(third), equalTo(ABSENT));
		assertThat(newArrayList(store.stored()), containsInAnyOrder(first, second));
	}

	@Test
	public void missingElementRemoved() throws Exception {
		// given
		final Lockable first = card(1L);

		// when
		store.remove(first);

		// then
		assertThat(store.isPresent(first), equalTo(false));
		assertThat(store.get(first), equalTo(ABSENT));
	}

	@Test
	public void elementRemoved() throws Exception {
		// given
		final Lockable first = card(1L);
		final DummyLock firstMetadata = new DummyLock();
		final Lockable second = card(2L);
		final DummyLock secondMetadata = new DummyLock();

		// when
		store.add(first, firstMetadata);
		store.add(second, secondMetadata);
		store.remove(first);

		// then
		assertThat(store.isPresent(first), equalTo(false));
		assertThat(store.get(first), equalTo(ABSENT));
		assertThat(store.isPresent(second), equalTo(true));
		assertThat(store.get(second), equalTo(of(secondMetadata)));
	}

	@Test
	public void allElementsRemoved() throws Exception {
		// given
		final Lockable first = card(1L);
		final DummyLock firstMetadata = new DummyLock();
		final Lockable second = card(2L);
		final DummyLock secondMetadata = new DummyLock();

		// when
		store.add(first, firstMetadata);
		store.add(second, secondMetadata);
		store.removeAll();

		// then
		assertThat(store.isPresent(first), equalTo(false));
		assertThat(store.get(first), equalTo(ABSENT));
		assertThat(store.isPresent(second), equalTo(false));
		assertThat(store.get(first), equalTo(ABSENT));
	}

	@Test
	public void allElementsRemovedIteratingOverStoredOnes() throws Exception {
		// given
		final Lockable first = card(1L);
		final DummyLock firstMetadata = new DummyLock();
		final Lockable second = card(2L);
		final DummyLock secondMetadata = new DummyLock();

		// when
		store.add(first, firstMetadata);
		store.add(second, secondMetadata);
		for (final Lockable lockable : store.stored()) {
			store.remove(lockable);
		}

		// then
		assertThat(store.isPresent(first), equalTo(false));
		assertThat(store.get(first), equalTo(ABSENT));
		assertThat(store.isPresent(second), equalTo(false));
		assertThat(store.get(first), equalTo(ABSENT));
	}

}
