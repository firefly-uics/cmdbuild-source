package integration.logic.bim;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static org.cmdbuild.services.bim.DefaultBimDataModelManager.PROJECTID;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
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
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;

import utils.IntegrationTestBimBase;

import com.google.common.collect.Lists;

;

public class BindCardsToProjectsTest extends IntegrationTestBimBase {

	private static final String CLASS_NAME = "Edificio";
	private static final String PROJECTS_CLASS = BimProjectStorableConverter.TABLE_NAME;
	private DataDefinitionLogic dataDefinitionLogic;
	private BimLogic bimLogic;

	private CMClass testClass;
	private CMClass projectsClass;

	@Before
	public void setUp() throws Exception {

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

		bimLogic = new BimLogic(bimServiceFacade, bimDataPersistence,
				bimDataModelManager, null);
		projectsClass = dbDataView().findClass(PROJECTS_CLASS);

		// create a class
		testClass = dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));
	}

	@Test
	public void thereAreNoRelationsOnTheBimDomain() throws Exception {
		// given
		final String projectId = RandomStringUtils.randomAlphanumeric(5);
		final CMCard projectCard = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg" + projectId) //
				.set("Name", "Pg" + projectId) //
				.set(PROJECTID, projectId).save();

		// when
		bimLogic.updateBimLayer(CLASS_NAME, "root", "true");
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(
				projectCard.getId().toString(), CLASS_NAME);

		// then
		assertTrue(bindedCardsId.size() == 0);
	}

	@Test
	public void bindTwoCardsToOneProjectStartingWithAnEmptyBimDomain()
			throws Exception {
		// given
		final String projectId = RandomStringUtils.randomAlphanumeric(5);
		final CMCard projectCard = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg" + projectId) //
				.set("Name", "Pg" + projectId) //
				.set(PROJECTID, projectId).save();
		final String code1 = RandomStringUtils.randomAlphanumeric(5);
		final String code2 = RandomStringUtils.randomAlphanumeric(5);
		final CMCard firstCard = dbDataView().createCardFor(testClass) //
				.setCode(code1) //
				.save();

		final CMCard secondCard = dbDataView().createCardFor(testClass) //
				.setCode(code2) //
				.save();

		bimLogic.updateBimLayer(CLASS_NAME, "root", "true");

		final String id1 = firstCard.getId().toString();
		final String id2 = secondCard.getId().toString();
		ArrayList<String> cardsId = Lists.newArrayList();
		cardsId.add(id1);
		cardsId.add(id2);
		final String projectCardId = projectCard.getId().toString();

		// when
		bimLogic.bindProjectToCards(projectCardId, cardsId);

		// then
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(
				projectCard.getId().toString(), CLASS_NAME);
		assertTrue(bimLogic.readBimLayer().get(0).getClassName()
				.equals(CLASS_NAME));
		assertTrue(bindedCardsId.size() == 2);
		assertTrue(bindedCardsId.contains(id1) && bindedCardsId.contains(id2));
	}

	@Test
	public void bindTwoCardsToOneProjectWithOneOfTheCardAlreadyBinded()
			throws Exception {
		// given
		final String projectId = RandomStringUtils.randomAlphanumeric(5);
		final CMCard projectCard = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg" + projectId) //
				.set("Name", "Pg-" + projectId) //
				.set("ProjectId", projectId).save();

		final String code1 = RandomStringUtils.randomAlphanumeric(5);
		final String code2 = RandomStringUtils.randomAlphanumeric(5);

		final CMCard firstCard = dbDataView().createCardFor(testClass) //
				.setCode(code1) //
				.save();

		final CMCard secondCard = dbDataView().createCardFor(testClass) //
				.setCode(code2) //
				.save();

		bimLogic.updateBimLayer(CLASS_NAME, "root", "true");
		final String projectCardId = projectCard.getId().toString();
		final String id1 = firstCard.getId().toString();
		ArrayList<String> cardsId = Lists.newArrayList();
		cardsId.add(id1);
		bimLogic.bindProjectToCards(projectCardId, cardsId);

		// when
		final String id2 = secondCard.getId().toString();
		cardsId.add(id2);
		bimLogic.bindProjectToCards(projectCardId, cardsId);

		// then
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(
				projectCard.getId().toString(), CLASS_NAME);
		assertTrue(bimLogic.readBimLayer().get(0).getClassName()
				.equals(CLASS_NAME));
		assertTrue(bindedCardsId.size() == 2);
		assertTrue(bindedCardsId.contains(id1) && bindedCardsId.contains(id2));
	}

	@Test
	public void bindNoneCardTWitSomeCardAlreadyBinded() throws Exception {
		// given
		final String projectId = RandomStringUtils.randomAlphanumeric(5);
		final CMCard projectCard = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg" + projectId) //
				.set("Name", "Pg-" + projectId) //
				.set("ProjectId", projectId).save();

		final String code1 = RandomStringUtils.randomAlphanumeric(5);
		final String code2 = RandomStringUtils.randomAlphanumeric(5);

		final CMCard firstCard = dbDataView().createCardFor(testClass) //
				.setCode(code1) //
				.save();

		final CMCard secondCard = dbDataView().createCardFor(testClass) //
				.setCode(code2) //
				.save();

		bimLogic.updateBimLayer(CLASS_NAME, "root", "true");
		final String projectCardId = projectCard.getId().toString();
		final String id1 = firstCard.getId().toString();
		final String id2 = secondCard.getId().toString();
		ArrayList<String> cardsId = Lists.newArrayList();
		cardsId.add(id1);
		cardsId.add(id2);
		bimLogic.bindProjectToCards(projectCardId, cardsId);

		// when
		cardsId.remove(id1);
		cardsId.remove(id2);
		bimLogic.bindProjectToCards(projectCardId, cardsId);

		// then
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(
				projectCardId, CLASS_NAME);
		assertTrue(bindedCardsId.size() == 0);
	}

	@Test(expected = DuplicateKeyException.class)
	public void tryToBindACardToTwoProjects() throws Exception {
		// given
		final String projectId1 = RandomStringUtils.randomAlphanumeric(5);
		final String projectId2 = RandomStringUtils.randomAlphanumeric(5);
		final CMCard projectCard1 = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg" + projectId1) //
				.set("Name", "Pg-" + projectId1) //
				.set("ProjectId", projectId1).save();
		final CMCard projectCard2 = dbDataView().createCardFor(projectsClass) //
				.setDescription("Pg" + projectId2) //
				.set("Name", "Pg-" + projectId2) //
				.set("ProjectId", projectId2).save();
		final String code1 = RandomStringUtils.randomAlphanumeric(5);
		final CMCard card1 = dbDataView().createCardFor(testClass) //
				.setCode(code1) //
				.save();

		bimLogic.updateBimLayer(CLASS_NAME, "root", "true");

		final String id1 = card1.getId().toString();
		ArrayList<String> cardsId = Lists.newArrayList();
		final String projectCardId1 = projectCard1.getId().toString();
		cardsId.add(id1);
		bimLogic.bindProjectToCards(projectCardId1, cardsId);

		// when
		final String projectCardId2 = projectCard2.getId().toString();
		bimLogic.bindProjectToCards(projectCardId2, cardsId);

		// then

	}

}
