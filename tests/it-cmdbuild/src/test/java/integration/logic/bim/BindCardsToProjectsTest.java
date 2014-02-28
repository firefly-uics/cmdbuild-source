package integration.logic.bim;

import static org.cmdbuild.services.bim.DefaultBimDataModelManager.PROJECTID;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.data.converter.BimProjectStorableConverter;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import utils.DatabaseDataFixture;
import utils.IntegrationTestBase;
import utils.IntegrationTestBim;
import utils.DatabaseDataFixture.Context;
import utils.DatabaseDataFixture.Hook;

import com.google.common.collect.Lists;
import com.mchange.util.AssertException;

public class BindCardsToProjectsTest extends IntegrationTestBim {

	private static final String PROJECTS_CLASS = BimProjectStorableConverter.TABLE_NAME;
	private CMClass projectsClass;
	
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
		projectsClass = dbDataView().findClass(PROJECTS_CLASS);
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
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(projectCard.getId().toString(), CLASS_NAME);

		// then
		assertTrue(bindedCardsId.size() == 0);
	}

	@Test
	public void bindTwoCardsToOneProjectStartingWithAnEmptyBimDomain() throws Exception {
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
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(projectCard.getId().toString(), CLASS_NAME);
		assertTrue(bimLogic.readBimLayer().get(0).getClassName().equals(CLASS_NAME));
		assertTrue(bindedCardsId.size() == 2);
		assertTrue(bindedCardsId.contains(id1) && bindedCardsId.contains(id2));
	}

	@Test
	public void bindTwoCardsToOneProjectWithOneOfTheCardAlreadyBinded() throws Exception {
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
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(projectCard.getId().toString(), CLASS_NAME);
		assertTrue(bimLogic.readBimLayer().get(0).getClassName().equals(CLASS_NAME));
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
		List<String> bindedCardsId = bimLogic.readBindingProjectToCards(projectCardId, CLASS_NAME);
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
