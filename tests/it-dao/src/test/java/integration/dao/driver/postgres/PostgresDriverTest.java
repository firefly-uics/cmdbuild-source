package integration.dao.driver.postgres;

import static com.google.common.collect.Iterables.isEmpty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static utils.IntegrationTestUtils.namesOf;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.reference.CMReference;
import org.junit.Ignore;
import org.junit.Test;

import utils.IntegrationTestBase;

/**
 * Tests specific to the legacy PostgreSQL driver
 */
public class PostgresDriverTest extends IntegrationTestBase {

	private final static String CLASSNAME_CONTAINING_REGCLASS = "Menu";
	private final static String TEXT_MANDATORY_ATTRIBUTE = "Type";
	private final static String REGCLASS_ATTRIBUTE = "IdElementClass";

	@Test
	public void theBaseClassIsAlwaysThere() {
		/*
		 * There are a dozen classes in the empty database...
		 * 
		 * At least this comes in handy for the tests!
		 */
		final Iterable<DBClass> allClasses = dbDriver().findAllClasses();
		assertThat(isEmpty(allClasses), equalTo(false));
		assertThat(namesOf(allClasses), hasItem(Constants.BASE_CLASS_NAME));
	}

	@Ignore("until we are able to add a non-reserved regclass attribute")
	@Test
	public void regclassAttributesAreReadFromTheDatabase() {
		final DBClass classWithRegClassAttribute = dbDriver().findClass(CLASSNAME_CONTAINING_REGCLASS);
		for (final DBAttribute attribute : classWithRegClassAttribute.getAttributes()) {
			System.out.println(attribute.getName() + " " + attribute.getType());
		}
		DBCard.newInstance(dbDriver(), classWithRegClassAttribute) //
				.set(TEXT_MANDATORY_ATTRIBUTE, "Anything not null") //
				.set(REGCLASS_ATTRIBUTE, classWithRegClassAttribute.getId()) //
				.save();

		final CMQueryRow row = new QuerySpecsBuilder(dbDataView()) //
				.select(REGCLASS_ATTRIBUTE) //
				.from(classWithRegClassAttribute) //
				.run().getOnlyRow();

		final CMReference reference = (CMReference) row.getCard(classWithRegClassAttribute).get(REGCLASS_ATTRIBUTE);

		assertThat(reference.getId(), is(classWithRegClassAttribute.getId()));
	}

}
