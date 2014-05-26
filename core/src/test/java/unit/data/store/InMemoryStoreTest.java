package unit.data.store;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.InMemoryStore;
import org.cmdbuild.data.store.Storable;
import org.junit.Before;
import org.junit.Test;

public class InMemoryStoreTest {

	private static class StorableTestDouble implements Storable {

		public static StorableTestDouble of(final String identifier) {
			return new StorableTestDouble(identifier);
		}

		public static StorableTestDouble of(final String identifier, final String value) {
			return new StorableTestDouble(identifier, value);
		}

		private final String identifier;
		private final String value;

		private StorableTestDouble(final String identifier) {
			this(identifier, null);
		}

		private StorableTestDouble(final String identifier, final String value) {
			this.identifier = identifier;
			this.value = value;
		}

		@Override
		public String getIdentifier() {
			return identifier;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			return identifier.hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof StorableTestDouble)) {
				return false;
			}
			final StorableTestDouble other = StorableTestDouble.class.cast(obj);
			return identifier.equals(other.identifier) && value.equals(other.value);
		}

		@Override
		public String toString() {
			return identifier;
		}

	}

	private static final StorableTestDouble FOO = StorableTestDouble.of("foo");
	private static final StorableTestDouble BAR = StorableTestDouble.of("bar");
	private static final StorableTestDouble BAZ = StorableTestDouble.of("baz");

	private InMemoryStore<StorableTestDouble> store;

	@Before
	public void setUp() throws Exception {
		store = InMemoryStore.of(StorableTestDouble.class);
	}

	@Test
	public void elementCreatedAndRead() throws Exception {
		store.create(FOO);

		final StorableTestDouble readed = store.read(FOO);

		assertThat(readed, equalTo(FOO));
	}

	@Test
	public void multipleElementsCreatedAndRead() throws Exception {
		store.create(FOO);
		store.create(BAR);
		store.create(BAZ);

		final Iterable<StorableTestDouble> elements = store.readAll();

		assertThat(elements, containsInAnyOrder(FOO, BAR, BAZ));
	}

	@Test
	public void elementCreatedUpdatedAndRead() throws Exception {
		store.create(StorableTestDouble.of("foo", "foo"));
		store.update(StorableTestDouble.of("foo", "bar"));

		final StorableTestDouble readed = store.read(FOO);

		assertThat(readed.getValue(), equalTo("bar"));
	}

	@Test
	public void elementsAddedAndDeleted() throws Exception {
		store.create(FOO);
		store.create(BAR);
		store.delete(FOO);

		final Iterable<StorableTestDouble> elements = store.readAll();

		assertThat(elements, containsInAnyOrder(BAR));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void elementsAreNotGroupableYet() throws Exception {
		store.readAll(new Groupable() {

			@Override
			public String getGroupAttributeName() {
				return "foo";
			}

			@Override
			public Object getGroupAttributeValue() {
				return "bar";
			}

		});
	}

}
