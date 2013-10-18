package utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.context.SystemPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.lookup.DataViewLookupStore;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStorableConverter;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.spring.SpringIntegrationUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Class containing methods for initializing the integration tests database
 */
public abstract class IntegrationTestBase {

	protected static final DBInitializer dbInitializer = new DBInitializer();

	private final DBDriver testDriver;
	private final DBDataView dbView;

	protected IntegrationTestBase() {
		this.testDriver = createTestDriver();
		this.dbView = new DBDataView(testDriver);
	}

	/**
	 * Override if you need to decorate the default.
	 */
	protected DBDriver createBaseDriver() {
		return dbInitializer.getDriver();
	}

	/**
	 * Override if you don't need/want the rollback driver.
	 */
	protected DBDriver createTestDriver() {
		return new GenericRollbackDriver(createBaseDriver());
	}

	public DBDriver dbDriver() {
		return testDriver;
	}

	public DBDataView dbDataView() {
		return dbView;
	}

	public OperationUser operationUser() {
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		final PrivilegeContext privilegeContext = new SystemPrivilegeContext();
		final CMGroup group = mock(CMGroup.class);
		return new OperationUser(authenticatedUser, privilegeContext, group);
	}

	public LookupStore lookupStore() {
		final DataViewStore<Lookup> store = new DataViewStore<Lookup>(dbView, new LookupStorableConverter());
		return new DataViewLookupStore(store);
	}

	public UserStore userStore() {
		return new UserStore() {

			private OperationUser operationUser;

			@Override
			public OperationUser getUser() {
				return operationUser;
			}

			@Override
			public void setUser(final OperationUser operationUser) {
				this.operationUser = operationUser;
			}

		};
	}

	public UserStore userStore(final OperationUser operationUser) {
		final UserStore userStore = userStore();
		userStore.setUser(operationUser);
		return userStore;
	}

	@BeforeClass
	public static void initialize() {
		final ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-context.xml");
		new SpringIntegrationUtils().setApplicationContext(applicationContext);

		dbInitializer.initialize();
	}

	@Before
	// FIXME see comment
	/*
	 * This method is needed until all (at least main) Spring issues will be
	 * resolved. Spring has been used improperly (with knowledge of the problem
	 * or not) for injecting components in the middle of code so hiding
	 * dependencies
	 */
	public void mockApplicationContext() {
		final ApplicationContext mockApplicartionContext = mock(ApplicationContext.class);
		when(mockApplicartionContext.getBean(DBDataView.class)) //
				.thenReturn(dbDataView());
		final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				new String[] { "application-context.xml" }, mockApplicartionContext);
		new SpringIntegrationUtils().setApplicationContext(applicationContext);
	}

	@After
	public final void rollback() {
		if (testDriver instanceof GenericRollbackDriver) {
			GenericRollbackDriver.class.cast(testDriver).rollback();
		}
	}

}
