package integration.logic.bim;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
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
import org.cmdbuild.services.bim.DefaultBimDataModelManager;
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

public class MapperCreateTest extends IntegrationTestBim {

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
	public void writeOneCardOnAnEmptyClass() throws Exception {
		// given
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity("Edificio");
		List<Attribute> attributeList = e.getAttributes();

		final String codeEdificio = "E" + RandomStringUtils.randomAlphanumeric(5);
		final String globalId = RandomStringUtils.randomAlphanumeric(22);

		attributeList.add(new BimAttribute("Code", codeEdificio));
		attributeList.add(new BimAttribute("Description", "Edificio 1"));
		attributeList.add(new BimAttribute("GlobalId", globalId));
		source.add(e);

		// when
		mapper.update(source);

		// then
		CMClass theClass = dbDataView().findClass(CLASS_NAME);
		CMQueryResult queryResult = dbDataView().select(attribute(theClass, DESCRIPTION_ATTRIBUTE))
		//
				.from(theClass)
				//
				.where(condition(attribute(theClass, CODE), eq(codeEdificio))).run();
		assertTrue(queryResult != null);
		CMCard card = queryResult.getOnlyRow().getCard(theClass);
		assertThat(card.getDescription().toString(), equalTo("Edificio 1"));

		CMClass bimClass = dbDataView().findClass(BimIdentifier.newIdentifier().withName(CLASS_NAME));
		queryResult = dbDataView().select(attribute(bimClass, "GlobalId"), attribute(bimClass, "Master")) //
				.from(bimClass) //
				.run();

		assertTrue(queryResult != null);
		CMCard bimCard = queryResult.getOnlyRow().getCard(bimClass);
		assertThat(bimCard.get("GlobalId").toString(), equalTo(globalId));
		assertThat(card.getId(), equalTo(bimCard.get("Master", IdAndDescription.class).getId()));
	}

	@Test
	public void createOneCardEdificioAndOneCardPianoAndTheReferenceBetweenThem() throws Exception {

		// given
		List<Entity> source1 = Lists.newArrayList();
		Entity edificio = new BimEntity(CLASS_NAME);
		List<Attribute> attributeList = edificio.getAttributes();
		String edificioGuid = "edificioGuid";
		attributeList.add(new BimAttribute("GlobalId", edificioGuid));
		attributeList.add(new BimAttribute("Code", "E1"));
		attributeList.add(new BimAttribute("Description", "Edificio 1"));
		source1.add(edificio);

		mapper.update(source1);

		List<Entity> source2 = Lists.newArrayList();
		Entity piano = new BimEntity(OTHER_CLASS_NAME);
		attributeList = piano.getAttributes();
		String pianoGuid = "pianoGuid";
		attributeList.add(new BimAttribute("GlobalId", pianoGuid));
		attributeList.add(new BimAttribute("Code", "P1"));
		attributeList.add(new BimAttribute("Description", "Piano 1"));
		attributeList.add(new BimAttribute("Edificio", edificioGuid));
		source2.add(piano);

		// when
		mapper.update(source2);

		// then
		CMClass theClass = dbDataView().findClass(OTHER_CLASS_NAME);
		CMQueryResult queryResult = dbDataView().select(attribute(theClass, DESCRIPTION_ATTRIBUTE)) //
				.from(theClass) //
				.run();
		assertTrue(queryResult != null);
		CMCard card = queryResult.getOnlyRow().getCard(theClass);
		assertThat(card.getDescription().toString(), equalTo("Piano 1"));

		CMClass bimClass = dbDataView().findClass(BimIdentifier.newIdentifier().withName(OTHER_CLASS_NAME));
		queryResult = dbDataView()
				.select(attribute(bimClass, "GlobalId"), attribute(bimClass, DefaultBimDataModelManager.FK_COLUMN_NAME)) //
				.from(bimClass) //
				.run();

		assertTrue(queryResult != null);
		CMCard bimCard = queryResult.getOnlyRow().getCard(bimClass);
		assertThat(bimCard.get("GlobalId").toString(), equalTo(pianoGuid));
		assertThat(card.getId(), equalTo(bimCard.get(DefaultBimDataModelManager.FK_COLUMN_NAME, IdAndDescription.class)
				.getId()));
	}

	// TODO
	// It would be nice having some more tests with LookUp attributes.

}
