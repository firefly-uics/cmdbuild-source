package integration.driver;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class DataTest extends QueryTestFixture {

	public DataTest(String driverBeanName) {
		super(driverBeanName);
	}

	private static final String A_CLASS_NAME = "A";

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
