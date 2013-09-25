package integration.logic.bim;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newAttribute;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
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
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import utils.DatabaseDataFixture;
import utils.IntegrationTestBase;
import utils.IntegrationTestBim;
import utils.DatabaseDataFixture.Context;
import utils.DatabaseDataFixture.Hook;

import com.google.common.collect.Lists;
import com.mchange.util.AssertException;

public class MapperCharAttributesTest extends IntegrationTestBim {

	private static final String ATTRIBUTE_NAME = "TheAttribute";
	
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
		// create one char attribute
		dataDefinitionLogic.createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("CHAR")));
	}

	@Test
	public void createCardWithCharAttribute() throws Exception {
		// given
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity("Edificio");
		List<Attribute> attributeList = e.getAttributes();

		final String edificioCode = "E" + RandomStringUtils.randomAlphanumeric(5);
		final String globalId = RandomStringUtils.randomAlphanumeric(22);

		attributeList.add(new BimAttribute("Code", edificioCode));
		attributeList.add(new BimAttribute("Description", "Edificio 1"));
		attributeList.add(new BimAttribute("GlobalId", globalId));
		attributeList.add(new BimAttribute(ATTRIBUTE_NAME, "A"));
		source.add(e);

		// when
		mapper.update(source);

		// then
		CMClass theClass = dbDataView().findClass(CLASS_NAME);
		CMQueryResult queryResult = dbDataView().select(anyAttribute(theClass)) //
				.from(theClass) //
				.where(condition(attribute(theClass, CODE), eq(edificioCode))) //
				.run();
		assertTrue(queryResult != null);
		CMCard card = queryResult.getOnlyRow().getCard(theClass);
		assertThat(card.getDescription().toString(), equalTo("Edificio 1"));
		assertThat(card.getCode().toString(), equalTo(edificioCode));
		assertThat(card.get(ATTRIBUTE_NAME).toString(), equalTo("A"));
	}

	@Test
	public void updateCardWithCharAttribute() throws Exception {
		// given
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity("Edificio");
		List<Attribute> attributeList = e.getAttributes();

		final String edificioCode = "E" + RandomStringUtils.randomAlphanumeric(5);
		final String globalId = RandomStringUtils.randomAlphanumeric(22);

		attributeList.add(new BimAttribute("Code", edificioCode));
		attributeList.add(new BimAttribute("Description", "Edificio 1"));
		attributeList.add(new BimAttribute("GlobalId", globalId));
		attributeList.add(new BimAttribute(ATTRIBUTE_NAME, "A"));
		source.add(e);

		mapper.update(source);

		attributeList.clear();
		attributeList.add(new BimAttribute("GlobalId", globalId));
		attributeList.add(new BimAttribute(ATTRIBUTE_NAME, "b"));

		// when
		mapper.update(source);

		// then
		CMClass theClass = dbDataView().findClass(CLASS_NAME);
		CMQueryResult queryResult = dbDataView().select(anyAttribute(theClass)) //
				.from(theClass) //
				.where(condition(attribute(theClass, CODE), eq(edificioCode))) //
				.run();
		assertTrue(queryResult != null);
		CMCard card = queryResult.getOnlyRow().getCard(theClass);
		assertThat(card.getDescription().toString(), equalTo("Edificio 1"));
		assertThat(card.getCode().toString(), equalTo(edificioCode));
		assertThat(card.get(ATTRIBUTE_NAME).toString(), equalTo("b"));
	}

}
