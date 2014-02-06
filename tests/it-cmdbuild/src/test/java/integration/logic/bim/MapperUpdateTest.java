package integration.logic.bim;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import utils.DatabaseDataFixture;
import utils.DatabaseDataFixture.Context;
import utils.DatabaseDataFixture.Hook;
import utils.IntegrationTestBase;
import utils.IntegrationTestBim;

import com.google.common.collect.Lists;
import com.mchange.util.AssertException;

public class MapperUpdateTest extends IntegrationTestBim {

	@ClassRule
	public static DatabaseDataFixture databaseDataFixture = DatabaseDataFixture.newInstance() //
			.dropAfter(true) //
			.hook(new Hook() {

				@Override
				public void before(final Context context) {
					try {
						final JdbcTemplate jdbcTemplate = new JdbcTemplate(context.dataSource());
						final URL url = IntegrationTestBase.class.getClassLoader().getResource("postgis.sql");
						final String sql = FileUtils.readFileToString(new File(url.toURI()));
						jdbcTemplate.execute(sql);
					} catch (Exception e) {
						e.printStackTrace();
						throw new AssertException("should never come here");
					}
				}

				@Override
				public void after(final Context context) {
					// do nothing
				}

			}) //
			.build();

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void updateOneCard() throws Exception {
		// given
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity("Edificio");
		List<Attribute> attributeList = e.getAttributes();

		attributeList.add(new BimAttribute(CODE, "E1"));
		attributeList.add(new BimAttribute(DESCRIPTION, "Edificio 1"));
		final String guid = RandomStringUtils.randomAlphanumeric(22);
		attributeList.add(new BimAttribute(GLOBAL_ID, guid));
		source.add(e);

		// fill testClass with one card
		final CMCard oldCard = dbDataView().createCardFor(testClass) //
				.setCode("E0") //
				.setDescription("Edificio 0") //
				.save();

		CMClass bimTestClass = dbDataView().findClass(BimIdentifier.newIdentifier().withName(CLASS_NAME));
		dbDataView().createCardFor(bimTestClass) //
				.set(GLOBAL_ID, guid) //
				.set("Master", oldCard.getId()) //
				.save();

		// when
		mapper.update(source);

		// then
		CMClass theClass = dbDataView().findClass(CLASS_NAME);
		CMQueryResult queryResult = dbDataView().select(attribute(theClass, DESCRIPTION_ATTRIBUTE)) //
				.from(theClass) //
				.run();
		assertTrue(queryResult != null);
		CMCard card = queryResult.getOnlyRow().getCard(theClass);
		assertThat(card.getDescription().toString(), equalTo("Edificio 1"));

		CMClass bimClass = dbDataView().findClass(BimIdentifier.newIdentifier().withName(CLASS_NAME));
		queryResult = dbDataView().select(attribute(bimClass, GLOBAL_ID), attribute(bimClass, "Master")) //
				.from(bimClass) //
				.run();

		assertTrue(queryResult != null);
		CMCard bimCard = queryResult.getOnlyRow().getCard(bimClass);
		assertThat(bimCard.get(GLOBAL_ID).toString(), equalTo(guid));
		assertThat(card.getId(), equalTo(bimCard.get("Master", IdAndDescription.class).getId()));
	}

	@Test
	public void createTwoCardsEdificioAndOneCardPianoAndTryToChangeTheReferenceFromOneEdificioToTheOther()
			throws Exception {

		String codePiano1 = "P1-" + RandomStringUtils.randomAlphanumeric(5);
		String codePiano2 = "P2-" + RandomStringUtils.randomAlphanumeric(5);
		String codeEdificio1 = "E1-" + RandomStringUtils.randomAlphanumeric(5);
		String codeEdificio2 = "E2-" + RandomStringUtils.randomAlphanumeric(5);
		// given
		final CMCard e1 = dbDataView().createCardFor(testClass) //
				.setCode(codeEdificio1) //
				.setDescription("Edificio 1") //
				.save();
		String guid1 = RandomStringUtils.randomAlphanumeric(22);
		dbDataView().createCardFor(bimTestClass) //
				.set(GLOBAL_ID, guid1) //
				.set("Master", e1.getId()) //
				.save();

		final CMCard e2 = dbDataView().createCardFor(testClass) //
				.setCode(codeEdificio2) //
				.setDescription("Edificio 2") //
				.save();
		String guid2 = RandomStringUtils.randomAlphanumeric(22);
		dbDataView().createCardFor(bimTestClass) //
				.set(GLOBAL_ID, guid2) //
				.set("Master", e2.getId()) //
				.save();

		final CMCard p1 = dbDataView().createCardFor(otherClass) //
				.setCode(codePiano1) //
				.setDescription("Piano 1") //
				.set(CLASS_NAME, e1.getId().toString()) //
				.save();
		String guid3 = RandomStringUtils.randomAlphanumeric(22);
		dbDataView().createCardFor(bimOtherClass) //
				.set(GLOBAL_ID, guid3) //
				.set("Master", p1.getId()) //
				.save();

		List<Entity> source = Lists.newArrayList();
		Entity piano = new BimEntity(OTHER_CLASS_NAME);
		List<Attribute> attributeList = piano.getAttributes();
		attributeList.add(new BimAttribute(GLOBAL_ID, guid3));
		attributeList.add(new BimAttribute(CODE, codePiano2));
		attributeList.add(new BimAttribute(DESCRIPTION, "Piano 2"));
		attributeList.add(new BimAttribute(CLASS_NAME, guid2));
		source.add(piano);

		// when
		mapper.update(source);

		// then
		CMQueryResult result = dbDataView().select( //
				anyAttribute(otherClass)) //
				.from(otherClass) //
				.run();
		assertTrue(result != null);
		CMQueryRow row = result.getOnlyRow();
		CMCard card = row.getCard(otherClass);
		assertThat(card.getCode().toString(), equalTo(codePiano2));
		assertThat(card.getDescription().toString(), equalTo("Piano 2"));
		assertTrue(((IdAndDescription) card.get(CLASS_NAME)).getId() == e2.getId());

	}

	@Test
	public void createCardWithLookupAttribute() throws Exception {
		// given
		String code = "P2-" + RandomStringUtils.randomAlphanumeric(5);
		List<Entity> source = Lists.newArrayList();
		Entity piano = new BimEntity(OTHER_CLASS_NAME);
		List<Attribute> attributeList = piano.getAttributes();
		final String guid1 = RandomStringUtils.randomAlphanumeric(22);
		attributeList.add(new BimAttribute(GLOBAL_ID, guid1));
		attributeList.add(new BimAttribute(CODE, code));
		attributeList.add(new BimAttribute(DESCRIPTION, "Piano secondo"));
		attributeList.add(new BimAttribute(LOOKUP_TYPE_NAME, LOOKUP_VALUE2));
		source.add(piano);

		// when
		mapper.update(source);

		// then
		CMQueryResult result = dbDataView().select( //
				anyAttribute(otherClass)) //
				.from(otherClass) //
				.where(condition(attribute(otherClass, CODE), eq(code))) //
				.run();
		assertTrue(result != null);
		CMQueryRow row = result.getOnlyRow();
		CMCard card = row.getCard(otherClass);
		assertThat(card.getCode().toString(), equalTo(code));
		assertThat(card.getDescription().toString(), equalTo("Piano secondo"));
		assertThat(((IdAndDescription) card.get(LOOKUP_TYPE_NAME)).getDescription(), equalTo(LOOKUP_VALUE2));
	}

	@Test
	public void updateOneLookupAttribute() throws Exception {
		// given
		LookupType type = LookupType.newInstance().withName(LOOKUP_TYPE_NAME).build();

		Long lookupValue1Id = null;
		Iterable<Lookup> allOfType = lookupStore().listForType(type);
		for (Iterator<Lookup> it = allOfType.iterator(); it.hasNext();) {
			Lookup l = it.next();
			if (l.getDescription() != null && l.getDescription().equals(LOOKUP_VALUE1)) {
				lookupValue1Id = l.getId();
				break;
			}
		}

		String codePiano1 = "P1-" + RandomStringUtils.randomAlphanumeric(5);
		String codePiano2 = "P2-" + RandomStringUtils.randomAlphanumeric(5);
		final CMCard p1 = dbDataView().createCardFor(otherClass)//
				.setCode(codePiano1)//
				.setDescription("Primo piano")//
				.set(LOOKUP_TYPE_NAME, lookupValue1Id) //
				.save();

		String guid1 = RandomStringUtils.randomAlphanumeric(22);
		dbDataView().createCardFor(bimOtherClass) //
				.set(GLOBAL_ID, guid1) //
				.set("Master", p1.getId()) //
				.save();

		List<Entity> source = Lists.newArrayList();
		Entity piano = new BimEntity(OTHER_CLASS_NAME);
		List<Attribute> attributeList = piano.getAttributes();
		attributeList.add(new BimAttribute(GLOBAL_ID, guid1));
		attributeList.add(new BimAttribute(CODE, codePiano2));
		attributeList.add(new BimAttribute(DESCRIPTION, "Piano secondo"));
		attributeList.add(new BimAttribute(LOOKUP_TYPE_NAME, LOOKUP_VALUE2));
		source.add(piano);

		// when
		mapper.update(source);

		// then
		CMQueryResult result = dbDataView().select( //
				anyAttribute(otherClass)) //
				.from(otherClass) //
				.where(condition(attribute(otherClass, CODE), eq(codePiano2))) //
				.run();
		assertTrue(result != null);
		CMQueryRow row = result.getOnlyRow();
		CMCard card = row.getCard(otherClass);
		assertThat(card.getCode().toString(), equalTo(codePiano2));
		assertThat(card.getDescription().toString(), equalTo("Piano secondo"));
		assertThat(((IdAndDescription) card.get(LOOKUP_TYPE_NAME)).getDescription(), equalTo(LOOKUP_VALUE2));

	}

}
