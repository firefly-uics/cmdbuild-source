package integration.logic.bim;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newAttribute;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static integration.logic.data.DataDefinitionLogicTest.newDomain;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CardReference;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
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
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import utils.IntegrationTestBase;

import com.google.common.collect.Lists;

public class MapperCreateTest extends IntegrationTestBase {

	private static final String CLASS_NAME = "Edificio";
	private static final String OTHER_CLASS_NAME = "Piano";
	private DataDefinitionLogic dataDefinitionLogic;
	private BimLogic bimLogic;
	private CMClass testClass;
	private CMClass otherClass;

	@Before
	public void setUp() throws Exception {

		// create the logic
		BimService bimservice = mock(BimService.class);
		BimServiceFacade bimServiceFacade = new DefaultBimServiceFacade(bimservice);
		dataDefinitionLogic = new DefaultDataDefinitionLogic(dbDataView());
		DataViewStore<BimProjectInfo> projectInfoStore = new DataViewStore<BimProjectInfo>(dbDataView(),
				new BimProjectStorableConverter());
		DataViewStore<BimLayer> mapperInfoStore = new DataViewStore<BimLayer>(dbDataView(), new BimLayerStorableConverter());
		BimDataPersistence bimDataPersistence = new DefaultBimDataPersistence(projectInfoStore, mapperInfoStore);
		BimDataModelManager bimDataModelManager = new DefaultBimDataModelManager(dbDataView(), dataDefinitionLogic, null, null);
		bimLogic = new BimLogic(bimServiceFacade, bimDataPersistence, bimDataModelManager);

		// create the classes
		testClass = dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));
		bimLogic.updateBimLayer(CLASS_NAME, "active", "true");

		otherClass = dataDefinitionLogic.createOrUpdate(a(newClass(OTHER_CLASS_NAME)));
		bimLogic.updateBimLayer(OTHER_CLASS_NAME, "active", "true");

		// create the domain
		final CMDomain domain = dataDefinitionLogic.create(a(newDomain(CLASS_NAME + OTHER_CLASS_NAME) //
				.withIdClass1(otherClass.getId()) //
				.withIdClass2(testClass.getId()) //
				.withCardinality(CARDINALITY_N1.value()) //
				));

		// create the reference
		dataDefinitionLogic.createOrUpdate( //
				a(newAttribute(CLASS_NAME) //
						.withOwnerName(otherClass.getIdentifier().getLocalName()) //
						.withType("REFERENCE") //
						.withDomain(domain.getIdentifier().getLocalName())));
	}

	@Ignore
	// RollbackDriver throws exceptions during the After.
	@Test
	public void writeOneCardOnAnEmptyClass() throws Exception {
		// given
		Mapper mapper = new Mapper(dbDataView(), null);
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity("Edificio");
		List<Attribute> attributeList = e.getAttributes();

		attributeList.add(new BimAttribute("Code", "E1"));
		attributeList.add(new BimAttribute("Description", "Edificio 1"));
		String newGuid = "newGuid";
		attributeList.add(new BimAttribute("GlobalId", newGuid));
		source.add(e);

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
		queryResult = dbDataView().select(attribute(bimClass, "GlobalId"), attribute(bimClass, "Master")) //
				.from(bimClass) //
				.run();

		assertTrue(queryResult != null);
		CMCard bimCard = queryResult.getOnlyRow().getCard(bimClass);
		assertThat(bimCard.get("GlobalId").toString(), equalTo(newGuid));
		assertThat(card.getId(), equalTo(bimCard.get("Master", CardReference.class).getId()));
	}

	@Ignore
	// RollbackDriver throws exceptions during the After.
	@Test
	public void createOneCardEdificioAndOneCardPianoAndTheReferenceBetweenThem() throws Exception {

		// given
		Mapper mapper = new Mapper(dbDataView(), null);
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
		assertThat(card.getId(), equalTo(bimCard.get(DefaultBimDataModelManager.FK_COLUMN_NAME, CardReference.class)
				.getId()));
	}
	
	// TODO
	// It would be nice having some more tests with LookUp attributes.

}
