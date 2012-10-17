package integration.driver;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class DataTest extends DriverFixture {

	public DataTest(final String driverBeanName) {
		super(driverBeanName);
	}

	private static final String A_CLASS_NAME = uniqueUUID();

	protected static final String ATTRIBUTE_1 = org.cmdbuild.dao.driver.postgres.Const.CODE_ATTRIBUTE;
	protected static final String ATTRIBUTE_2 = org.cmdbuild.dao.driver.postgres.Const.DESCRIPTION_ATTRIBUTE;

	private static final Object ATTRIBUTE_1_VALUE = "foo";
	private static final Object ATTRIBUTE_2_VALUE = "bar";

	@Test
	public void cardsCanBeAdded() {
		final DBClass newClass = driver.createClass(A_CLASS_NAME, null);
		final CMCard newCard = DBCard.newInstance(driver, newClass) //
				.set(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.set(ATTRIBUTE_2, ATTRIBUTE_2_VALUE) //
				.save();

		assertThat(newCard.getId(), is(notNullValue()));
		assertThat(newCard.get(ATTRIBUTE_1), equalTo(ATTRIBUTE_1_VALUE));
		assertThat(newCard.get(ATTRIBUTE_2), equalTo(ATTRIBUTE_2_VALUE));
	}

}
