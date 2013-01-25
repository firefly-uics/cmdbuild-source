package unit.driver;

import static org.junit.Assert.*;

import java.util.List;

import org.cmdbuild.dao.TypeObjectCache;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Before;
import org.junit.Test;
import org.cmdbuild.dao.driver.AbstractDBDriver.DefaultTypeObjectCache;

public class DefaultTypeObjectCacheTest {

	private DBClass foo;
	private DBClass bar;
	private DBClass baz;
	private TypeObjectCache cache;
	
	public static class ClientThread extends Thread {
		
		private final TypeObjectCache cache;
		
		public ClientThread(TypeObjectCache cache) {
			this.cache = cache;
		}
		
		@Override
		public void run() {
			DBClass clazz = DBClass.newClass().withName("thread" + getId()).withId(getId()).build();
			cache.add(clazz);
		}
	}
	
	
	@Before
	public void setUp() {
		foo = DBClass.newClass().withName("foo").withId(Long.valueOf(1)).build();
		bar = DBClass.newClass().withName("bar").withId(Long.valueOf(2)).build();
		baz = DBClass.newClass().withName("baz").withId(Long.valueOf(3)).build();
		cache = new DefaultTypeObjectCache();
	}
	
	@Test
	public void cacheShouldHaveNoClassesIfItIsOnlyInitialized() {
		//when
		boolean cacheHaveNoClasses = cache.hasNoClass();
		
		//then
		assertTrue(cacheHaveNoClasses);
	}
	
	@Test
	public void cacheShouldNotBeEmptyIfClassesAdded() {
		//when
		cache.add(foo);
		List<DBClass> cachedClasses = cache.fetchCachedClasses();
		
		//then
		assertFalse(cache.hasNoClass());
		assertEquals(1, cachedClasses.size());
	}
	
	@Test
	public void clearCacheShouldWorkCorrectly() {
		//when
		cache.add(foo);
		cache.add(bar);
		cache.add(baz);
		cache.clearCache();
		
		//then
		assertTrue(cache.hasNoClass());
	}

}
