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
	private static final String A_SUPERCLASS_NAME = uniqueUUID();

	protected static final String CODE_ATTRIBUTE = org.cmdbuild.dao.driver.postgres.Const.CODE_ATTRIBUTE;
	protected static final String DESCRIPTION_ATTRIBUTE = org.cmdbuild.dao.driver.postgres.Const.DESCRIPTION_ATTRIBUTE;

	private static final Object CODE_VALUE = "foo";
	private static final Object DESCRIPTION_VALUE = "bar";

	@Test
	public void cardsCanBeAdded() {
		final DBClass newClass = driver.createClass(A_CLASS_NAME, null);
		final CMCard newCard = DBCard.newInstance(driver, newClass) //
				.set(CODE_ATTRIBUTE, CODE_VALUE) //
				.set(DESCRIPTION_ATTRIBUTE, DESCRIPTION_VALUE) //
				.save();

		assertThat(newCard.getId(), is(notNullValue()));
		assertThat(newCard.get(CODE_ATTRIBUTE), equalTo(CODE_VALUE));
		assertThat(newCard.get(DESCRIPTION_ATTRIBUTE), equalTo(DESCRIPTION_VALUE));
	}

	@Test(expected = Exception.class)
	public void cardsCannotBeAddedInSuperclass() {
		final DBClass newClass = driver.createSuperClass(A_SUPERCLASS_NAME, null);
		final CMCard newCard = DBCard.newInstance(driver, newClass) //
				.set(CODE_ATTRIBUTE, CODE_VALUE) //
				.set(DESCRIPTION_ATTRIBUTE, DESCRIPTION_VALUE) //
				.save();
	}

}
