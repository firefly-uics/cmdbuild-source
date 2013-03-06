package unit.driver;

import static org.cmdbuild.dao.entrytype.DBIdentifier.fromName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cmdbuild.dao.TypeObjectCache;
import org.cmdbuild.dao.driver.AbstractDBDriver.DefaultTypeObjectCache;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Before;
import org.junit.Test;

public class DefaultTypeObjectCacheTest {

	private DBClass foo;
	private DBClass bar;
	private DBClass baz;
	private TypeObjectCache cache;

	public static class ClientThread extends Thread {

		private final TypeObjectCache cache;

		public ClientThread(final TypeObjectCache cache) {
			this.cache = cache;
		}

		@Override
		public void run() {
			final DBClass clazz = DBClass.newClass().withIdentifier(fromName("thread" + getId())).withId(getId())
					.build();
			cache.add(clazz);
		}
	}

	@Before
	public void setUp() {
		foo = DBClass.newClass().withIdentifier(fromName("foo")).withId(Long.valueOf(1)).build();
		bar = DBClass.newClass().withIdentifier(fromName("bar")).withId(Long.valueOf(2)).build();
		baz = DBClass.newClass().withIdentifier(fromName("baz")).withId(Long.valueOf(3)).build();
		cache = new DefaultTypeObjectCache();
	}

	@Test
	public void cacheShouldHaveNoClassesIfItIsOnlyInitialized() {
		// when
		final boolean cacheHaveNoClasses = cache.hasNoClass();

		// then
		assertTrue(cacheHaveNoClasses);
	}

	@Test
	public void cacheShouldNotBeEmptyIfClassesAdded() {
		// when
		cache.add(foo);
		final List<DBClass> cachedClasses = cache.fetchCachedClasses();

		// then
		assertFalse(cache.hasNoClass());
		assertEquals(1, cachedClasses.size());
	}

	@Test
	public void clearCacheShouldWorkCorrectly() {
		// when
		cache.add(foo);
		cache.add(bar);
		cache.add(baz);
		cache.clearCache();

		// then
		assertTrue(cache.hasNoClass());
	}

}
