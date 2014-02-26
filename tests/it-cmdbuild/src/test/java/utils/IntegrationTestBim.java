package utils;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newAttribute;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static integration.logic.data.DataDefinitionLogicTest.newDomain;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.context.SystemPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.BimLayerStorableConverter;
import org.cmdbuild.data.converter.BimProjectStorableConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
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
import org.cmdbuild.utils.bim.BimIdentifier;
import org.junit.ClassRule;
import org.springframework.jdbc.core.JdbcTemplate;

import utils.DatabaseDataFixture;
import utils.DatabaseDataFixture.Context;
import utils.DatabaseDataFixture.Hook;
import utils.IntegrationTestBase;

import com.mchange.util.AssertException;

public abstract class IntegrationTestBim {

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

	protected static final String LOOKUP_VALUE1 = "L1";
	protected static final String LOOKUP_VALUE2 = "L2";
	protected static final String GLOBAL_ID = "GlobalId";
	protected static final String DESCRIPTION = "Description";
	protected static final String CODE = "Code";
	protected static final String CLASS_NAME = "Edificio";
	protected static final String OTHER_CLASS_NAME = "Piano";
	protected static final String LOOKUP_TYPE_NAME = "Livello";
	protected DataDefinitionLogic dataDefinitionLogic;
	protected BimLogic bimLogic;
	protected CMClass testClass;
	protected CMClass otherClass;
	protected CMClass bimTestClass;
	protected CMClass bimOtherClass;
	protected Mapper mapper;

	protected LookupLogic lookupLogic() {
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		final PrivilegeContext privilegeCtx = new SystemPrivilegeContext();
		final CMGroup cmGroup = mock(CMGroup.class);
		final OperationUser operationUser = new OperationUser(authenticatedUser, privilegeCtx, cmGroup);
		return new LookupLogic(lookupStore(), operationUser, dbDataView());
	}

	protected void setUp() throws Exception {

		System.out.println("Build the data model");

		// create the logic
		BimService bimservice = mock(BimService.class);
		BimServiceFacade bimServiceFacade = new DefaultBimServiceFacade(bimservice);
		dataDefinitionLogic = new DefaultDataDefinitionLogic(dbDataView());
		DataViewStore<BimProjectInfo> projectInfoStore = DataViewStore.newInstance(dbDataView(),
				new BimProjectStorableConverter());
		DataViewStore<BimLayer> mapperInfoStore = DataViewStore.newInstance(dbDataView(),
				new BimLayerStorableConverter());
		BimDataPersistence bimDataPersistence = new DefaultBimDataPersistence(projectInfoStore, mapperInfoStore);
		BimDataModelManager bimDataModelManager = new DefaultBimDataModelManager(dbDataView(), dataDefinitionLogic,
				null, jdbcTemplate().getDataSource());
		mapper = new BimMapper(dbDataView(), lookupLogic(), databaseDataFixture.dataSource());
		bimLogic = new BimLogic(bimServiceFacade, bimDataPersistence, bimDataModelManager, mapper, null, null, null);

		// create the classes
		testClass = dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));
		bimLogic.updateBimLayer(CLASS_NAME, "active", "true");
		bimTestClass = dbDataView().findClass(BimIdentifier.newIdentifier().withName(CLASS_NAME));

		otherClass = dataDefinitionLogic.createOrUpdate(a(newClass(OTHER_CLASS_NAME)));
		bimLogic.updateBimLayer(OTHER_CLASS_NAME, "active", "true");
		bimOtherClass = dbDataView().findClass(BimIdentifier.newIdentifier().withName(OTHER_CLASS_NAME));

		// create the domain
		final String domainName = CLASS_NAME + OTHER_CLASS_NAME;
		CMDomain domain = dbDataView().findDomain(domainName);
		if (domain == null) {
			domain = dataDefinitionLogic.create(a(newDomain(CLASS_NAME + OTHER_CLASS_NAME) //
					.withIdClass1(otherClass.getId()) //
					.withIdClass2(testClass.getId()) //
					.withCardinality(CARDINALITY_N1.value()) //
					));
		}

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

		lookupLogic().saveLookupType(newType, oldType);

		lookupLogic().createOrUpdateLookup(Lookup.newInstance() //
				.withDescription(LOOKUP_VALUE1) //
				.withType(newType) //
				.withActiveStatus(true) //
				.build());

		lookupLogic().createOrUpdateLookup(Lookup.newInstance() //
				.withDescription(LOOKUP_VALUE2) //
				.withType(newType) //
				.withActiveStatus(true) //
				.build());

		// create a lookup attribute
		dataDefinitionLogic.createOrUpdate(//
				a(newAttribute(LOOKUP_TYPE_NAME) //
						.withOwnerName(OTHER_CLASS_NAME) //
						.withType("LOOKUP") //
						.withLookupType(LOOKUP_TYPE_NAME)));

	}

	/*
	 * Utils
	 */

	protected JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(databaseDataFixture.dataSource());
	}

	protected CMDataView dbDataView() {
		return databaseDataFixture.systemDataView();
	}

	protected LookupStore lookupStore() {
		return databaseDataFixture.lookupStore();
	}

}
