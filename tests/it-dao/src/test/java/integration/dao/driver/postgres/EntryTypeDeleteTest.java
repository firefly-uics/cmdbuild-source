package integration.dao.driver.postgres;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static utils.IntergrationTestUtils.newClass;
import static utils.IntergrationTestUtils.newSimpleClass;
import static utils.IntergrationTestUtils.newTextAttribute;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import utils.IntegrationTestBase;

public class EntryTypeDeleteTest extends IntegrationTestBase {

	private DBClass clazz;

	/**
	 * We don't want default rollback driver here.
	 */
	@Override
	protected DBDriver createTestDriver() {
		return createBaseDriver();
	}

	@Test
	public void cardForStandarClassSuccessfullyDeleted() {
		// given
		final DBClass parent = dbDataView().findClassByName(Constants.BASE_CLASS_NAME);
		clazz = dbDataView().createClass(newClass("foo", parent));
		final DBCard card = (DBCard) dbDataView().newCard(clazz) //
				.setCode("foo") //
				.save();

		// when
		// FIXME use the data view
		dbDriver().delete(card);
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.run();

		// then
		assertThat(result.size(), equalTo(0));
	}

	@Ignore("fix queries on simple tables")
	@Test
	public void cardForSimpleClassSuccessfullyDeleted() {
		// given
		clazz = dbDataView().createClass(newSimpleClass("foo"));
		dbDataView().createAttribute(newTextAttribute("Code", clazz));
		final DBCard card = (DBCard) dbDataView().newCard(clazz) //
				.setCode("foo") //
				.save();

		// when
		// FIXME use the data view
		dbDriver().delete(card);
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.run();

		// then
		assertThat(result.size(), equalTo(0));
	}

	@After
	public void deleteEntryTypes() throws Exception {
		dbDataView().clear(clazz);
		dbDataView().deleteClass(clazz);
	}

}
