package integration.logic.bim;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.data.converter.BimLayerConverter;
import org.cmdbuild.data.converter.BimProjectStorableConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.logic.bim.BIMLogic;
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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;

import utils.IntegrationTestBase;

import com.google.common.collect.Lists;

public class BindCardsToProjectsTest extends IntegrationTestBase {

	private static final String CLASS_NAME = "Edificio";
	private static final String PROJECTS_CLASS = BimProjectStorableConverter.TABLE_NAME;
	private DataDefinitionLogic dataDefinitionLogic;
	private BIMLogic bimLogic;

	private CMClass testClass;
	private CMClass projectsClass;

	@Before
	public void createDefaultTest() throws Exception {

		BimService bimservice = mock(BimService.class);
		BimServiceFacade bimServiceFacade = new DefaultBimServiceFacade(bimservice);
		dataDefinitionLogic = new DefaultDataDefinitionLogic(dbDataView());

		DataViewStore<BimProjectInfo> projectInfoStore = new DataViewStore<BimProjectInfo>(dbDataView(),
				new BimProjectStorableConverter());
		DataViewStore<BimLayer> mapperInfoStore = new DataViewStore<BimLayer>(dbDataView(),
				new BimLayerConverter());
		BimDataPersistence bimDataPersistence = new DefaultBimDataPersistence(projectInfoStore, mapperInfoStore);
		BimDataModelManager bimDataModelManager = new DefaultBimDataModelManager(dbDataView(), dataDefinitionLogic, null);

		bimLogic = new BIMLogic(bimServiceFacade, bimDataPersistence, bimDataModelManager);

		projectsClass = dbDataView().findClass(PROJECTS_CLASS);
		testClass = dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));
	}

	@Test
	public void thereAreNoRelationsOnTheBimDomain() throws Exception {

		// given
		final CMCard projectCard = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg1") //
				.set("Name", "Pg-1") //
				.set("ProjectId", "456").save();

		// when
		bimLogic.updateBimLayer(CLASS_NAME, "root", "true");
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(projectCard.getId().toString(), CLASS_NAME);

		// then
		assertTrue(bindedCardsId.size() == 0);
	}

	@Test
	public void bindTwoCardsToOneProjectStartingWithAnEmptyBimDomain() throws Exception {
		// given
		final CMCard projectCard = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg1") //
				.set("Name", "Pg-1") //
				.set("ProjectId", "123").save();

		final CMCard firstCard = dbDataView().createCardFor(testClass) //
				.setCode("c1") //
				.save();

		final CMCard secondCard = dbDataView().createCardFor(testClass) //
				.setCode("c2") //
				.save();

		bimLogic.updateBimLayer(CLASS_NAME, "root", "true"); // crea il
																		// dominio

		final String projectId = projectCard.getId().toString();
		final String id1 = firstCard.getId().toString();
		final String id2 = secondCard.getId().toString();
		ArrayList<String> cardsId = Lists.newArrayList();
		cardsId.add(id1);
		cardsId.add(id2);

		// when
		bimLogic.bindProjectToCards(projectId, cardsId);

		// then
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(projectCard.getId().toString(), CLASS_NAME);
		assertTrue(bimLogic.readBimLayer().get(0).getClassName().equals(CLASS_NAME));
		assertTrue(bindedCardsId.size() == 2);
		assertTrue(bindedCardsId.contains(id1) && bindedCardsId.contains(id2));

	}

	/**
	 * This test rises an exception when the rollback is performed in the
	 * @After. The rollback driver can not manage the case in which entries are
	 * deleted.
	 **/
	@Ignore
	@Test
	public void bindTwoCardsToOneProjectWithOneOfTheCardAlreadyBinded() throws Exception {
		// given
		final CMCard projectCard = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg1") //
				.set("Name", "Pg-1") //
				.set("ProjectId", "123").save();

		final CMCard firstCard = dbDataView().createCardFor(testClass) //
				.setCode("c1") //
				.save();

		final CMCard secondCard = dbDataView().createCardFor(testClass) //
				.setCode("c2") //
				.save();

		bimLogic.updateBimLayer(CLASS_NAME, "root", "true");
		final String projectId = projectCard.getId().toString();
		final String id1 = firstCard.getId().toString();
		ArrayList<String> cardsId = Lists.newArrayList();
		cardsId.add(id1);
		bimLogic.bindProjectToCards(projectId, cardsId);

		// when
		final String id2 = secondCard.getId().toString();
		cardsId.add(id2);
		bimLogic.bindProjectToCards(projectId, cardsId);

		// then
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(projectCard.getId().toString(), CLASS_NAME);
		assertTrue(bimLogic.readBimLayer().get(0).getClassName().equals(CLASS_NAME));
		assertTrue(bindedCardsId.size() == 2);
		assertTrue(bindedCardsId.contains(id1) && bindedCardsId.contains(id2));
	}

	/**
	 * This test rises an exception when the rollback is performed in the
	 * @After. The rollback driver can not manage the case in which entries are
	 * deleted.
	 **/
	@Ignore
	@Test
	public void bindNoneCardTWitSomeCardAlreadyBinded() throws Exception {
		// given
		final CMCard projectCard = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg1") //
				.set("Name", "Pg-1") //
				.set("ProjectId", "123").save();

		final CMCard firstCard = dbDataView().createCardFor(testClass) //
				.setCode("c1") //
				.save();

		final CMCard secondCard = dbDataView().createCardFor(testClass) //
				.setCode("c2") //
				.save();

		bimLogic.updateBimLayer(CLASS_NAME, "root", "true");
		final String projectId = projectCard.getId().toString();
		final String id1 = firstCard.getId().toString();
		final String id2 = secondCard.getId().toString();
		ArrayList<String> cardsId = Lists.newArrayList();
		cardsId.add(id1);
		cardsId.add(id2);
		bimLogic.bindProjectToCards(projectId, cardsId);

		// when
		cardsId.remove(id1);
		cardsId.remove(id2);
		bimLogic.bindProjectToCards(projectId, cardsId);

		// then
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(projectCard.getId().toString(), CLASS_NAME);
		assertTrue(bindedCardsId.size() == 0);
	}

	@Test(expected = DuplicateKeyException.class)
	public void tryToBindACardToTwoProjects() throws Exception {
		// given
		final CMCard projectCard1 = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg1") //
				.set("Name", "Pg-1") //
				.set("ProjectId", "123").save();
		final CMCard projectCard2 = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg2") //
				.set("Name", "Pg-2") //
				.set("ProjectId", "456").save();
		final CMCard card1 = dbDataView().createCardFor(testClass) //
				.setCode("c1") //
				.save();
		
		bimLogic.updateBimLayer(CLASS_NAME, "root", "true");
		
		final String id1 = card1.getId().toString();
		ArrayList<String> cardsId = Lists.newArrayList();
		final String projectCardId1 = projectCard1.getId().toString();
		cardsId.add(id1);
		bimLogic.bindProjectToCards(projectCardId1, cardsId);
		
		//when
		final String projectCardId2 = projectCard2.getId().toString();
		bimLogic.bindProjectToCards(projectCardId2, cardsId);
		
		//then
		
	}

}
