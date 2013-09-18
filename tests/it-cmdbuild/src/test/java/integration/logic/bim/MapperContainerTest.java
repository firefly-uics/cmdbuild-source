package integration.logic.bim;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static org.cmdbuild.bim.utils.BimConstants.IFC_GLOBALID;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
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
import org.cmdbuild.services.bim.connector.BimMapper;
import org.cmdbuild.services.bim.connector.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import utils.IntegrationTestBimBase;

import com.google.common.collect.Lists;

public class MapperContainerTest extends IntegrationTestBimBase {

	private static final String CLASS_NAME = "Computer";
	private static final String CONTAINER_CLASS = "Room";
	private DataDefinitionLogic dataDefinitionLogic;
	private BimLogic bimLogic;
	private LookupLogic lookupLogic = mock(LookupLogic.class);
	private JdbcTemplate jdbcTemplate;
	private Mapper mapper;

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
		mapper = new BimMapper(dbDataView(), lookupLogic(), dataSource());
		bimLogic = new BimLogic(bimServiceFacade, bimDataPersistence,
				bimDataModelManager, mapper);

		// create the class
		dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));

		// create BIM-table and geometry column
		bimLogic.updateBimLayer(CLASS_NAME, "active", "true");
		bimLogic.updateBimLayer(CLASS_NAME, "export", "true");
	}

	@Test
	public void setContainer() throws Exception {
		// given
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity("Computer");
		List<Attribute> attributeList = e.getAttributes();

		final String computerCode = "C" + RandomStringUtils.random(5);
		final String globalId = RandomStringUtils.random(22);

		attributeList.add(new BimAttribute(CODE_ATTRIBUTE, computerCode));
		attributeList
				.add(new BimAttribute(DESCRIPTION_ATTRIBUTE, "Computer 1"));
		attributeList.add(new BimAttribute(IFC_GLOBALID, globalId));

		// FIXME not managed yet!!!
		attributeList.add(new BimAttribute(CONTAINER_CLASS, globalId));
		source.add(e);

		// when
		mapper.update(source);

		// then
		System.out.println("");
	}
}
