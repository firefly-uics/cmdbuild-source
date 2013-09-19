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

import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBim;

import com.google.common.collect.Lists;

public class MapperDecimalAttributesTest extends IntegrationTestBim {

	private static final String ATTRIBUTE_NAME = "TheAttribute";

	@Before
	public void setUp() throws Exception {
		super.setUp();

		// create one decimal attribute
		dataDefinitionLogic.createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("DECIMAL")//
						.withPrecision(5)//
						.withScale(3)));
	}

	@Test
	public void createCardWithDecimalAttribute() throws Exception {
		// given
		final String codeEdificio = "E" + RandomStringUtils.randomAlphanumeric(5);
		final String globalId = RandomStringUtils.randomAlphanumeric(22);
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity("Edificio");
		List<Attribute> attributeList = e.getAttributes();

		attributeList.add(new BimAttribute("Code", codeEdificio));
		attributeList.add(new BimAttribute("Description", "Edificio 1"));
		attributeList.add(new BimAttribute("GlobalId", globalId));
		attributeList.add(new BimAttribute(ATTRIBUTE_NAME, "10.5"));
		source.add(e);

		// when
		mapper.update(source);

		// then
		CMClass theClass = dbDataView().findClass(CLASS_NAME);
		CMQueryResult queryResult = dbDataView().select(anyAttribute(theClass)) //
				.from(theClass) //
				.where(condition(attribute(theClass, CODE), eq(codeEdificio))) //
				.run();
		assertTrue(queryResult != null);
		CMCard card = queryResult.getOnlyRow().getCard(theClass);
		assertThat(card.getDescription().toString(), equalTo("Edificio 1"));
		assertThat(card.getCode().toString(), equalTo(codeEdificio));
		assertThat(card.get(ATTRIBUTE_NAME).toString(), equalTo("10.500"));
	}

	@Test
	public void updateCardWithDecimalAttribute() throws Exception {
		// given
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity("Edificio");
		List<Attribute> attributeList = e.getAttributes();

		final String codeEdificio = "E" + RandomStringUtils.randomAlphanumeric(5);
		final String globalId = RandomStringUtils.randomAlphanumeric(22);

		attributeList.add(new BimAttribute("Code", codeEdificio));
		attributeList.add(new BimAttribute("Description", "Edificio 1"));
		attributeList.add(new BimAttribute("GlobalId", globalId));
		attributeList.add(new BimAttribute(ATTRIBUTE_NAME, "10.5"));
		source.add(e);

		mapper.update(source);

		attributeList.clear();
		attributeList.add(new BimAttribute("GlobalId", globalId));
		attributeList.add(new BimAttribute(ATTRIBUTE_NAME, "9.35"));

		// when
		mapper.update(source);

		// then
		CMClass theClass = dbDataView().findClass(CLASS_NAME);
		CMQueryResult queryResult = dbDataView().select(anyAttribute(theClass)) //
				.from(theClass) //
				.where(condition(attribute(theClass, CODE), eq(codeEdificio))) //
				.run();
		assertTrue(queryResult != null);
		CMCard card = queryResult.getOnlyRow().getCard(theClass);
		assertThat(card.getDescription().toString(), equalTo("Edificio 1"));
		assertThat(card.getCode().toString(), equalTo(codeEdificio));
		assertThat(card.get(ATTRIBUTE_NAME).toString(), equalTo("9.350"));
	}

}
