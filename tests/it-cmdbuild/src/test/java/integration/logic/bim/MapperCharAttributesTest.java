package integration.logic.bim;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newAttribute;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.data.converter.BimLayerStorableConverter;
import org.cmdbuild.data.converter.BimProjectStorableConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.DefaultBimDataModelManager;
import org.cmdbuild.services.bim.DefaultBimDataPersistence;
import org.cmdbuild.services.bim.DefaultBimServiceFacade;
import org.cmdbuild.services.bim.connector.BimMapper;
import org.cmdbuild.services.bim.connector.Mapper;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBimBase;

import com.google.common.collect.Lists;

public class MapperCharAttributesTest extends IntegrationTestBimBase {

	private static final String CLASS_NAME = "Edificio";
	private static final String ATTRIBUTE_NAME = "TheAttribute";
	private static final String CODE = "Code";
	private DataDefinitionLogic dataDefinitionLogic;
	private BimLogic bimLogic;
	private Mapper mapper;
	private CMClass testClass;

	@Before
	public void setUp() throws Exception {

		// create the logic
		BimService bimservice = mock(BimService.class);
		BimServiceFacade bimServiceFacade = new DefaultBimServiceFacade(
				bimservice);
		dataDefinitionLogic = new DefaultDataDefinitionLogic(dbDataView());
		DataViewStore<BimProjectInfo> projectInfoStore = new DataViewStore<BimProjectInfo>(
				dbDataView(), new BimProjectStorableConverter());
		DataViewStore<BimLayer> mapperInfoStore = new DataViewStore<BimLayer>(
				dbDataView(), new BimLayerStorableConverter());
		BimDataPersistence bimDataPersistence = new DefaultBimDataPersistence(
				projectInfoStore, mapperInfoStore);
		BimDataModelManager bimDataModelManager = new DefaultBimDataModelManager(
				dbDataView(), dataDefinitionLogic, null, jdbcTemplate()
						.getDataSource());
		mapper = new BimMapper(dbDataView(), lookupLogic(), dataSource());
		bimLogic = new BimLogic(bimServiceFacade, bimDataPersistence,
				bimDataModelManager, mapper);

		// create the classes
		testClass = dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));
		bimLogic.updateBimLayer(CLASS_NAME, "active", "true");

		// create one boolean attribute
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

		final String edificioCode = "E" + RandomStringUtils.random(5);
		final String globalId = RandomStringUtils.random(22);

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

		final String edificioCode = "E" + RandomStringUtils.random(5);
		final String globalId = RandomStringUtils.random(22);

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
