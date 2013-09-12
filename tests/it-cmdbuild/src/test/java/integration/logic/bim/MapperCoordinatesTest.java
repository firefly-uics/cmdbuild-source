package integration.logic.bim;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.services.bim.DefaultBimDataModelManager.BIM_SCHEMA;
import static org.cmdbuild.services.bim.DefaultBimDataModelManager.COORDINATES;
import static org.cmdbuild.services.bim.DefaultBimDataModelManager.GLOBALID;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.data.converter.BimLayerStorableConverter;
import org.cmdbuild.data.converter.BimProjectStorableConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.logic.bim.BimLogic;
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
import org.junit.Test;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import utils.IntegrationTestBimBase;

import com.google.common.collect.Lists;

public class MapperCoordinatesTest extends IntegrationTestBimBase {

	private static final String CLASS_NAME = "Edificio";
	private DataDefinitionLogic dataDefinitionLogic;
	private BimLogic bimLogic;
	private LookupLogic lookupLogic = mock(LookupLogic.class);
	private JdbcTemplate jdbcTemplate;

	@Before
	public void setUp() throws Exception {

		jdbcTemplate = ((PostgresDriver) dbDriver()).getJdbcTemplate();

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
				dbDataView(), dataDefinitionLogic, lookupLogic,
				jdbcTemplate.getDataSource());
		bimLogic = new BimLogic(bimServiceFacade, bimDataPersistence,
				bimDataModelManager);

		// create the class
		dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));

		// create bim-table and geometry column
		bimLogic.updateBimLayer(CLASS_NAME, "active", "true");
		bimLogic.updateBimLayer(CLASS_NAME, "export", "true");
	}

	@Test
	public void setCoordinates() throws Exception {
		// given
		Mapper mapper = new Mapper(dbDataView(), lookupLogic, jdbcTemplate);
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity("Edificio");
		List<Attribute> attributeList = e.getAttributes();

		attributeList.add(new BimAttribute(CODE_ATTRIBUTE, "E1"));
		attributeList
				.add(new BimAttribute(DESCRIPTION_ATTRIBUTE, "Edificio 1"));
		String newGuid = "newGuid";
		attributeList.add(new BimAttribute(GLOBALID, newGuid));
		attributeList.add(new BimAttribute("x1", "1.2"));
		attributeList.add(new BimAttribute("x2", "3.4"));
		attributeList.add(new BimAttribute("x3", "5.6"));
		source.add(e);

		// when
		mapper.update(source);

		// then
		CMClass bimClass = dbDataView().findClass(
				BimIdentifier.newIdentifier().withName(CLASS_NAME));

		CMQueryResult queryResult = dbDataView().select(anyAttribute(bimClass)) //
				.from(bimClass) //
				.run();
		assertTrue(queryResult != null);
		CMCard bimCard = queryResult.getOnlyRow().getCard(bimClass);
		assertThat(bimCard.get(GLOBALID).toString(), equalTo(newGuid));

		String SELECT_COORDINATES_TEMPLATE = "SELECT \"%s\" FROM %s.\"%s\" WHERE \"%s\" = %s";
		final String selectCoordinatesQuery = String.format(
				SELECT_COORDINATES_TEMPLATE, //
				COORDINATES, //
				BIM_SCHEMA, //
				CLASS_NAME, //
				ID_ATTRIBUTE, //
				bimCard.getId().toString());

		System.out.println(selectCoordinatesQuery);

		jdbcTemplate.query(selectCoordinatesQuery, new RowCallbackHandler() {
			Geometry coordinates = null;

			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				final String geometryAsString = rs.getString(COORDINATES);
				if (geometryAsString != null && !geometryAsString.equals("")) {
					coordinates = PGgeometry.geomFromString(geometryAsString);
					assertTrue(coordinates != null);
					System.out.println(coordinates.toString());
				}
			}
		});
	}
}
