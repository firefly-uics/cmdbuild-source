package integration.logic.bim;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newAttribute;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static integration.logic.data.DataDefinitionLogicTest.newDomain;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Iterator;
import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.context.SystemPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CardReference;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.data.converter.BimLayerConverter;
import org.cmdbuild.data.converter.BimProjectStorableConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.bim.BIMLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
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

public class MapperUpdateTest extends IntegrationTestBase {

	private static final String GUID1 = "guid1";
	private static final String LOOKUP_VALUE1 = "L1";
	private static final String LOOKUP_VALUE2 = "L2";
	private static final String GLOBAL_ID = "GlobalId";
	private static final String DESCRIPTION = "Description";
	private static final String CODE = "Code";
	private static final String CLASS_NAME = "Edificio";
	private static final String OTHER_CLASS_NAME = "Piano";
	private static final String LOOKUP_TYPE_NAME = "Livello";
	private DataDefinitionLogic dataDefinitionLogic;
	private BIMLogic bimLogic;
	private LookupLogic lookupLogic;
	private CMClass testClass;
	private CMClass otherClass;
	private DBClass bimTestClass;
	private DBClass bimOtherClass;

	@Before
	public void setUp() throws Exception {

		// create the logic
		BimService bimservice = mock(BimService.class);
		BimServiceFacade bimServiceFacade = new DefaultBimServiceFacade(bimservice);
		dataDefinitionLogic = new DefaultDataDefinitionLogic(dbDataView());
		DataViewStore<BimProjectInfo> projectInfoStore = new DataViewStore<BimProjectInfo>(dbDataView(),
				new BimProjectStorableConverter());
		DataViewStore<BimLayer> mapperInfoStore = new DataViewStore<BimLayer>(dbDataView(), new BimLayerConverter());
		BimDataPersistence bimDataPersistence = new DefaultBimDataPersistence(projectInfoStore, mapperInfoStore);
		BimDataModelManager bimDataModelManager = new DefaultBimDataModelManager(dbDataView(), dataDefinitionLogic,
				null);
		bimLogic = new BIMLogic(bimServiceFacade, bimDataPersistence, bimDataModelManager);

		// create the classes
		testClass = dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));
		bimLogic.updateBimLayer(CLASS_NAME, "active", "true");
		bimTestClass = dbDataView().findClass(BimIdentifier.newIdentifier().withName(CLASS_NAME));

		otherClass = dataDefinitionLogic.createOrUpdate(a(newClass(OTHER_CLASS_NAME)));
		bimLogic.updateBimLayer(OTHER_CLASS_NAME, "active", "true");
		bimOtherClass = dbDataView().findClass(BimIdentifier.newIdentifier().withName(OTHER_CLASS_NAME));

		// create the domain
		final CMDomain domain = dataDefinitionLogic.create(a(newDomain(CLASS_NAME + OTHER_CLASS_NAME) //
				.withIdClass1(otherClass.getId()) //
				.withIdClass2(testClass.getId()) //
				.withCardinality(CARDINALITY_N1.value()) //
				));

		// create the reference attribute
		dataDefinitionLogic.createOrUpdate( //
				a(newAttribute(CLASS_NAME) //
						.withOwnerName(otherClass.getIdentifier().getLocalName()) //
						.withType("REFERENCE") //
						.withDomain(domain.getIdentifier().getLocalName())));

		// create a lookup type and two values
		final LookupType newType = LookupType.newInstance() //
				.withName(LOOKUP_TYPE_NAME) //
				.build();
		final LookupType oldType = LookupType.newInstance().withName("").build();
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		final PrivilegeContext privilegeCtx = new SystemPrivilegeContext();
		final CMGroup cmGroup = mock(CMGroup.class);
		lookupLogic = new LookupLogic(lookupStore(), new OperationUser(authenticatedUser, privilegeCtx, cmGroup),
				dbDataView());

		lookupLogic.saveLookupType(newType, oldType);

		Lookup.newInstance() //
				.withDescription(LOOKUP_VALUE1) //
				.withType(newType) //
				.withActiveStatus(true) //
				.build();

		Lookup.newInstance() //
				.withDescription(LOOKUP_VALUE2) //
				.withType(newType) //
				.withActiveStatus(true) //
				.build();
		
		// create a lookup attribute
		dataDefinitionLogic.createOrUpdate(//
				a(newAttribute(LOOKUP_TYPE_NAME) //
						.withOwnerName(OTHER_CLASS_NAME) //
						.withType("LOOKUP") //
						.withLookupType(LOOKUP_TYPE_NAME)));

	}

	@Ignore
	// RollbackDriver throws exceptions during the After.
	@Test
	public void updateOneCard() throws Exception {
		// given
		Mapper mapper = new Mapper(dbDataView(), null);
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity("Edificio");
		List<Attribute> attributeList = e.getAttributes();

		attributeList.add(new BimAttribute(CODE, "E1"));
		attributeList.add(new BimAttribute(DESCRIPTION, "Edificio 1"));
		String newGuid = "newGuid";
		attributeList.add(new BimAttribute(GLOBAL_ID, newGuid));
		source.add(e);

		// fill testClass with one card
		final CMCard oldCard = dbDataView().createCardFor(testClass) //
				.setCode("E0") //
				.setDescription("Edificio 0") //
				.save();

		CMClass bimTestClass = dbDataView().findClass(BimIdentifier.newIdentifier().withName(CLASS_NAME));
		dbDataView().createCardFor(bimTestClass) //
				.set(GLOBAL_ID, newGuid) //
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
		assertThat(bimCard.get(GLOBAL_ID).toString(), equalTo(newGuid));
		assertThat(card.getId(), equalTo(bimCard.get("Master", CardReference.class).getId()));
	}

	@Ignore
	// RollbackDriver throws exceptions during the After.
	@Test
	public void createTwoCardsEdificioAndOneCardPianoAndTryToChangeTheReferenceFromOneEdificioToTheOther()
			throws Exception {

		// given
		final CMCard e1 = dbDataView().createCardFor(testClass) //
				.setCode("E1") //
				.setDescription("Edificio 1") //
				.save();
		String guid1 = GUID1;
		dbDataView().createCardFor(bimTestClass) //
				.set(GLOBAL_ID, guid1) //
				.set("Master", e1.getId()) //
				.save();

		final CMCard e2 = dbDataView().createCardFor(testClass) //
				.setCode("E2") //
				.setDescription("Edificio 2") //
				.save();
		String guid2 = "guid2";
		dbDataView().createCardFor(bimTestClass) //
				.set(GLOBAL_ID, guid2) //
				.set("Master", e2.getId()) //
				.save();

		final CMCard p1 = dbDataView().createCardFor(otherClass) //
				.setCode("P1") //
				.setDescription("Piano 1") //
				.set(CLASS_NAME, e1.getId().toString()) //
				.save();
		String guid3 = "guid3";
		dbDataView().createCardFor(bimOtherClass) //
				.set(GLOBAL_ID, guid3) //
				.set("Master", p1.getId()) //
				.save();

		Mapper mapper = new Mapper(dbDataView(), lookupLogic);
		List<Entity> source = Lists.newArrayList();
		Entity piano = new BimEntity(OTHER_CLASS_NAME);
		List<Attribute> attributeList = piano.getAttributes();
		attributeList.add(new BimAttribute(GLOBAL_ID, guid3));
		attributeList.add(new BimAttribute(CODE, "P1-new"));
		attributeList.add(new BimAttribute(DESCRIPTION, "Piano 1"));
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
		assertThat(card.getCode().toString(), equalTo("P1-new"));
		assertThat(card.getDescription().toString(), equalTo("Piano 1"));
		assertTrue(((CardReference) card.get(CLASS_NAME)).getId() == e2.getId());

	}
	
	@Ignore
	// RollbackDriver throws exceptions during the After.
	@Test
	public void createCardWithLookupAttribute() throws Exception {
		//given
		Mapper mapper = new Mapper(dbDataView(), lookupLogic);
		List<Entity> source = Lists.newArrayList();
		Entity piano = new BimEntity(OTHER_CLASS_NAME);
		List<Attribute> attributeList = piano.getAttributes();
		attributeList.add(new BimAttribute(GLOBAL_ID, GUID1));
		attributeList.add(new BimAttribute(CODE, "P2"));
		attributeList.add(new BimAttribute(DESCRIPTION, "Piano secondo"));
		attributeList.add(new BimAttribute(LOOKUP_TYPE_NAME, LOOKUP_VALUE2));
		source.add(piano);
		
		//when
		mapper.update(source);
		
		//then
		CMQueryResult result = dbDataView().select( //
				anyAttribute(otherClass)) //
				.from(otherClass) //
				.run();
		assertTrue(result != null);
		CMQueryRow row = result.getOnlyRow();
		CMCard card = row.getCard(otherClass);
		assertThat(card.getCode().toString(), equalTo("P2"));
		assertThat(card.getDescription().toString(), equalTo("Piano secondo"));
		assertThat(((CardReference) card.get(LOOKUP_TYPE_NAME)).getDescription(), equalTo(LOOKUP_VALUE2));
	}
	
	
	@Ignore
	// RollbackDriver throws exceptions during the After.
	@Test
	public void updateOneLookupAttribute() throws Exception {
		// given
		LookupType type = LookupType.newInstance().withName(LOOKUP_TYPE_NAME).build();
		Long lookupId = null;
		Iterable<Lookup> allOfType = lookupStore().listForType(type);
		for (Iterator<Lookup> it = allOfType.iterator(); it.hasNext();) {
			Lookup l = it.next();
			if (l.getDescription() != null && l.getDescription().equals(LOOKUP_VALUE1)) {
				lookupId = l.getId();
				break;
			}
		}

		final CMCard p1 = dbDataView().createCardFor(otherClass)//
				.setCode("P1")//
				.setDescription("Primo piano")//
				.set(LOOKUP_TYPE_NAME, lookupId) //
				.save();

		String guid1 = GUID1;
		dbDataView().createCardFor(bimOtherClass) //
				.set(GLOBAL_ID, guid1) //
				.set("Master", p1.getId()) //
				.save();

		Mapper mapper = new Mapper(dbDataView(), lookupLogic);
		List<Entity> source = Lists.newArrayList();
		Entity piano = new BimEntity(OTHER_CLASS_NAME);
		List<Attribute> attributeList = piano.getAttributes();
		attributeList.add(new BimAttribute(GLOBAL_ID, guid1));
		attributeList.add(new BimAttribute(CODE, "P2"));
		attributeList.add(new BimAttribute(DESCRIPTION, "Piano secondo"));
		attributeList.add(new BimAttribute(LOOKUP_TYPE_NAME, LOOKUP_VALUE2));
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
		assertThat(card.getCode().toString(), equalTo("P2"));
		assertThat(card.getDescription().toString(), equalTo("Piano secondo"));
		assertThat(((CardReference) card.get(LOOKUP_TYPE_NAME)).getDescription(), equalTo(LOOKUP_VALUE2));
		
		
	}

}
