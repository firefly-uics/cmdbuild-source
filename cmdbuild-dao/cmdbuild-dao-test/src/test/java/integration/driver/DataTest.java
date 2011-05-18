package integration.driver;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import utils.GenericRollbackDriver;

@RunWith(value = Parameterized.class)
public class DataTest {

	private static final String A_CLASS_NAME = "A";

	private static ApplicationContext appContext;

	static {
		appContext = new ClassPathXmlApplicationContext("structure-test-context.xml");
	}

	@Parameters
	public static Collection<Object[]> data() {
		final Collection<Object[]> params = new ArrayList<Object[]>();
		for (final String name : appContext.getBeanNamesForType(PostgresDriver.class)) {
			final Object[] o = { name };
			params.add(o);
		}
		return params;
	}

	private final GenericRollbackDriver driver;
	private final DBDriver driverToBeTested;

	public DataTest(final String driverBeanName) {
		driverToBeTested = appContext.getBean(driverBeanName, DBDriver.class);
		this.driver = new GenericRollbackDriver(driverToBeTested);
	}

	@After
	public void rollback() {
		driver.rollback();
	}

	/*
	 * Tests
	 */

	@Ignore
	@Test
	public void cardsCanBeAdded() {
		// given
		final DBClass newClass = driver.createClass(A_CLASS_NAME, null);
		// when
		final CMCard newCard = DBCard.create(driver, newClass)
			.set(org.cmdbuild.dao.driver.postgres.Utils.CODE_ATTRIBUTE, "Pizza").save(); // FIXME
		//then
		assertThat(newCard.getId(), is(notNullValue()));
	}
}
