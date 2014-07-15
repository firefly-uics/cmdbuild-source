package integration.dao;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.containsOrEquals;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.eq;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.isContainedWithinOrEquals;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newAttribute;
import static utils.IntegrationTestUtils.newClass;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

public class NetworkAttributeTypeQueryTest extends IntegrationTestBase {

	private static final String CLASS_NAME = "foo";
	private static final String ATTRIBUTE_NAME = "bar";

	private static final IpAddressAttributeType ATTRIBUTE_TYPE = new IpAddressAttributeType();

	private DBClass clazz;

	@Before
	public void createData() throws Exception {
		clazz = dbDataView().create(newClass(CLASS_NAME));
		dbDataView().createAttribute(newAttribute(ATTRIBUTE_NAME, ATTRIBUTE_TYPE, clazz));
	}

	@Test
	public void hostEqualWithClass() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.set(ATTRIBUTE_NAME, "192.168.1.1/32") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(attribute(clazz, ATTRIBUTE_NAME), eq("192.168.1.1/32"))) //
				.run();

		// then
		final CMQueryRow row = result.getOnlyRow();
		final CMCard card = row.getCard(clazz);
		assertThat((String) card.get(ATTRIBUTE_NAME), equalTo("192.168.1.1/32"));
	}

	@Test
	public void hostEqualWithoutClass() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.set(ATTRIBUTE_NAME, "192.168.1.1/32") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(attribute(clazz, ATTRIBUTE_NAME), eq("192.168.1.1"))) //
				.run();

		// then
		final CMQueryRow row = result.getOnlyRow();
		final CMCard card = row.getCard(clazz);
		assertThat((String) card.get(ATTRIBUTE_NAME), equalTo("192.168.1.1/32"));
	}

	@Test
	public void subnetEqualAnotherSubnet() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.set(ATTRIBUTE_NAME, "192.168.1.0/24") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(attribute(clazz, ATTRIBUTE_NAME), eq("192.168.1.0/24"))) //
				.run();

		// then
		final CMQueryRow row = result.getOnlyRow();
		final CMCard card = row.getCard(clazz);
		assertThat((String) card.get(ATTRIBUTE_NAME), equalTo("192.168.1.0/24"));
	}

	@Test
	public void hostContainedWithinSubnet() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.set(ATTRIBUTE_NAME, "192.168.1.1") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(attribute(clazz, ATTRIBUTE_NAME), isContainedWithinOrEquals("192.168.1.1/24"))) //
				.run();

		// then
		final CMQueryRow row = result.getOnlyRow();
		final CMCard card = row.getCard(clazz);
		assertThat((String) card.get(ATTRIBUTE_NAME), equalTo("192.168.1.1/32"));
	}

	@Test
	public void subnetContainedWithinAnotherSubnet() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.set(ATTRIBUTE_NAME, "192.168.1.0/24") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(attribute(clazz, ATTRIBUTE_NAME), isContainedWithinOrEquals("192.168.0.0/16"))) //
				.run();

		// then
		final CMQueryRow row = result.getOnlyRow();
		final CMCard card = row.getCard(clazz);
		assertThat((String) card.get(ATTRIBUTE_NAME), equalTo("192.168.1.0/24"));
	}

	@Test
	public void subnetContainsHost() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.set(ATTRIBUTE_NAME, "192.168.1.0/24") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(attribute(clazz, ATTRIBUTE_NAME), containsOrEquals("192.168.1.1/32"))) //
				.run();

		// then
		final CMQueryRow row = result.getOnlyRow();
		final CMCard card = row.getCard(clazz);
		assertThat((String) card.get(ATTRIBUTE_NAME), equalTo("192.168.1.0/24"));
	}

	@Test
	public void subnetContainsAnotherSubnet() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.set(ATTRIBUTE_NAME, "192.168.0.0/16") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(attribute(clazz, ATTRIBUTE_NAME), containsOrEquals("192.168.1.0/24"))) //
				.run();

		// then
		final CMQueryRow row = result.getOnlyRow();
		final CMCard card = row.getCard(clazz);
		assertThat((String) card.get(ATTRIBUTE_NAME), equalTo("192.168.0.0/16"));
	}

}
