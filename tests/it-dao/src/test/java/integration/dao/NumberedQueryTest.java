package integration.dao;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;

import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.junit.Test;

import utils.IntegrationTestBase;

public class NumberedQueryTest extends IntegrationTestBase {

	@Test
	public void numberedWithoutOrdering() {
		// given
		final DBClass foo = dbDataView().create(newClass("foo"));
		dbDataView().createCardFor(foo) //
				.setCode("foo") //
				.save();
		dbDataView().createCardFor(foo) //
				.setCode("bar") //
				.save();
		dbDataView().createCardFor(foo) //
				.setCode("baz") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(foo)) //
				.from(foo) //
				.numbered() //
				.run();
		final Iterable<CMQueryRow> rows = result;

		// then
		assertThat(size(rows), equalTo(3));
		assertThat(get(rows, 0).getCard(foo).getCode(), equalTo((Object) "foo"));
		assertThat(get(rows, 0).getNumber(), equalTo(1L));
		assertThat(get(rows, 1).getCard(foo).getCode(), equalTo((Object) "bar"));
		assertThat(get(rows, 1).getNumber(), equalTo(2L));
		assertThat(get(rows, 2).getCard(foo).getCode(), equalTo((Object) "baz"));
		assertThat(get(rows, 2).getNumber(), equalTo(3L));
	}

	@Test
	public void numberedWithOrdering() {
		// given
		final DBClass foo = dbDataView().create(newClass("foo"));
		dbDataView().createCardFor(foo) //
				.setCode("foo") //
				.save();
		dbDataView().createCardFor(foo) //
				.setCode("bar") //
				.save();
		dbDataView().createCardFor(foo) //
				.setCode("baz") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(foo)) //
				.from(foo) //
				.orderBy(foo.getCodeAttributeName(), Direction.DESC) //
				.numbered() //
				.run();
		final Iterable<CMQueryRow> rows = result;

		// then
		assertThat(size(rows), equalTo(3));
		assertThat(get(rows, 0).getCard(foo).getCode(), equalTo((Object) "foo"));
		assertThat(get(rows, 0).getNumber(), equalTo(1L));
		assertThat(get(rows, 1).getCard(foo).getCode(), equalTo((Object) "baz"));
		assertThat(get(rows, 1).getNumber(), equalTo(2L));
		assertThat(get(rows, 2).getCard(foo).getCode(), equalTo((Object) "bar"));
		assertThat(get(rows, 2).getNumber(), equalTo(3L));
	}

}
