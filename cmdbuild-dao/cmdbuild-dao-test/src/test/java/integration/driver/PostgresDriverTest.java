package integration.driver;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

import java.util.Collection;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.reference.CMReference;
import org.junit.Test;

/**
 * Tests specific to the legacy PostgreSQL driver
 */
public class PostgresDriverTest extends DriverFixture {

	private final static String REGCLASS_ENTRY_TYPE = "Menu";
	private final static String REGCLASS_ATTRIBUTE = "IdElementClass";

	public PostgresDriverTest() {
		super("pg_driver");
	}

	@Test
	public void theBaseClassIsAlwaysThere() {
		// There are a dozen classes in the empty database...
		// At least this comes in handy for the tests!
		final Collection<DBClass> allClasses = driver.findAllClasses();
		assertTrue(allClasses.size() > 0);
		assertThat(names(allClasses), hasItem(DBDriver.BASE_CLASS_NAME));
	}

	@Test
	public void regclassAttributesAreReadFromTheDatabase() {
		final DBClass menuClass = driver.findClassByName(REGCLASS_ENTRY_TYPE);
		DBCard.newInstance(driver, menuClass)
			.set("Type", "Anything not null")
			.set(REGCLASS_ATTRIBUTE, menuClass.getId()).save();

		final CMQueryRow row = new QuerySpecsBuilder(view)
			.select(REGCLASS_ATTRIBUTE).from(menuClass).run().getOnlyRow();

		final CMReference reference = (CMReference) row.getCard(menuClass).get(REGCLASS_ATTRIBUTE);

		assertThat(reference.getId(), is(menuClass.getId()));
	}
}
