package integration.dao;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.junit.After;
import org.junit.Test;

import utils.DBFixture;

public class EntryTypeClearTest extends DBFixture {

	private DBClass clazz;
	private DBDomain domain;

	/**
	 * We don't want default rollback driver here.
	 */
	@Override
	protected DBDriver createTestDriver() {
		return createBaseDriver();
	}

	@Test
	public void allCardsCleared() throws Exception {
		// given
		clazz = dbDataView().createClass(newClass("foo", null));
		dbDataView().newCard(clazz).setCode("this").save();
		dbDataView().newCard(clazz).setCode("is").save();
		dbDataView().newCard(clazz).setCode("a").save();
		dbDataView().newCard(clazz).setCode("test").save();

		// when
		dbDataView().clear(clazz);
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.run();

		// then
		assertThat(result.size(), equalTo(0));
	}

	@Test
	public void allRelationsCleared() throws Exception {
		// given
		clazz = dbDataView().createClass(newClass("foo", null));
		domain = dbDataView().createDomain(newDomain("bar", clazz, clazz));
		final CMCard card0 = dbDataView().newCard(clazz).setCode("baz").save();
		final CMCard card1 = dbDataView().newCard(clazz).setCode("baz").save();
		final CMCard card2 = dbDataView().newCard(clazz).setCode("baz").save();
		dbDataView().newRelation(domain).setCard1(card0).setCard2(card1).save();
		dbDataView().newRelation(domain).setCard1(card0).setCard2(card2).save();

		// when
		dbDataView().clear(domain);
		final Alias DST_ALIAS = Alias.as("DST");
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(domain)) //
				.from(clazz) //
				.join(anyClass(), as(DST_ALIAS), over(domain)) //
				.run();

		// then
		assertThat(result.size(), equalTo(0));
	}

	@After
	public void deleteEntryTypes() throws Exception {
		if (domain != null) {
			dbDataView().clear(domain);
			dbDataView().deleteDomain(domain);
		}
		if (clazz != null) {
			dbDataView().clear(clazz);
			dbDataView().deleteClass(clazz);
		}
	}

}
