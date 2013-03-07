package integration.logic.data.filter;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;
import static utils.IntegrationTestUtils.newSuperClass;
import static utils.IntegrationTestUtils.newTextAttribute;

import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.CardDTO;
import org.cmdbuild.logic.mapping.json.Constants.FilterOperator;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

public class SuperclassFilteredCardsTest extends FilteredCardsFixture {

	private DBClass root;

	@Override
	protected void initializeDatabaseData() {
		root = dbDataView().create(newSuperClass("root"));
		dbDataView().createAttribute(newTextAttribute("foo", root));
		final DBClass superNotRoot = dbDataView().create(newSuperClass("superNotRoot", root));
		final DBClass leafOfSuperNotRoot = dbDataView().create(newClass("leafOfSuperNotRoot", superNotRoot));
		final DBClass leafOfRoot = dbDataView().create(newClass("leafOfRoot", root));
		final DBClass anotherLeafOfRoot = dbDataView().create(newClass("anotherLeafOfRoot", root));

		dbDataView().createCardFor(leafOfSuperNotRoot) //
				.set("foo", leafOfSuperNotRoot.getName()) //
				.save();
		dbDataView().createCardFor(leafOfRoot) //
				.set("foo", leafOfRoot.getName()) //
				.save();
		dbDataView().createCardFor(anotherLeafOfRoot) //
				.set("foo", anotherLeafOfRoot.getName()) //
				.save();
	}

	@Override
	@After
	public void tearDown() {
		dbDataView().clear(root);
	}

	@Test
	public void cardsFilteredUsingSuperclassAttribute() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter("foo", FilterOperator.EQUAL, "leafOfSuperNotRoot");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<CardDTO> cards = dataAccessLogic.fetchCards(root.getName(), queryOptions).getPaginatedCards();

		// then
		assertThat(size(cards), equalTo(1));
		assertThat(get(cards, 0).getAttribute("foo"), equalTo((Object) "leafOfSuperNotRoot"));
	}

}
