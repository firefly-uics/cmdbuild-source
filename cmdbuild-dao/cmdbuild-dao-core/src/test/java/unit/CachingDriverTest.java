package unit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.cmdbuild.dao.driver.CachingDriver;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.InMemoryDriver;
import org.cmdbuild.dao.entrytype.DBClass;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;


public class CachingDriverTest {

	private final Mockery mockContext;
	private CachingDriver cachingDriver;

	public CachingDriverTest() {
		mockContext = new JUnit4Mockery();
	}

	@Test
	public void findAllClassesCachesTheResults() {
		final DBDriver innerDriver = mockContext.mock(DBDriver.class);
		cachingDriver =  new CachingDriver(innerDriver);

		final Collection<DBClass> allClasses = new ArrayList<DBClass>();

		mockContext.checking(new Expectations() {{
			oneOf(innerDriver).findAllClasses(); will(returnValue(allClasses));
		}});

		assertThat(cachingDriver.findAllClasses(), is(allClasses));
		assertThat(cachingDriver.findAllClasses(), is(allClasses));
	}

	@Test
	public void createClassAddsItOnBothDrivers() {
		final DBDriver innerDriver = new InMemoryDriver();
		cachingDriver = new CachingDriver(innerDriver);

		assertThat(innerDriver.findAllClasses().size(), is(0));

		cachingDriver.createClass("A", null);
		assertThat(innerDriver.findAllClasses().size(), is(1));
		assertThat(cachingDriver.findAllClasses().size(), is(1));

		cachingDriver.createClass("B", null);
		cachingDriver.createClass("C", null);
		assertThat(innerDriver.findAllClasses().size(), is(3));
		assertThat(cachingDriver.findAllClasses().size(), is(3));
	}

	@Test
	public void deleteClassDeletesItOnBothDrivers() {
		final DBDriver innerDriver = new InMemoryDriver();
		cachingDriver = new CachingDriver(innerDriver);

		DBClass a = innerDriver.createClass("A", null);
		DBClass b = innerDriver.createClass("B", null);
		DBClass c = innerDriver.createClass("C", null);
		assertThat(innerDriver.findAllClasses().size(), is(3));

		cachingDriver.deleteClass(a);
		assertThat(innerDriver.findAllClasses().size(), is(2));
		assertThat(cachingDriver.findAllClasses().size(), is(2));

		cachingDriver.deleteClass(b);
		cachingDriver.deleteClass(c);
		assertThat(innerDriver.findAllClasses().size(), is(0));
		assertThat(cachingDriver.findAllClasses().size(), is(0));
	}
}
