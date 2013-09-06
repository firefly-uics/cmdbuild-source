package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.lookup.DataViewLookupStore;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStorableConverter;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.auth.SoapAuthenticationLogicBuilder;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.access.lock.LockCardManager;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.services.cache.wrappers.CachingStore;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Data {

	@Autowired
	private DBDriver dbDriver;

	@Autowired
	private FilterStore filterStore;

	@Autowired
	private PrivilegeContextFactory privilegeContextFactory;

	@Autowired
	@Qualifier("soap")
	private AuthenticationService soapAuthenticationService;

	@Autowired
	@Qualifier("system")
	private LockCardManager systemLockCardManager;

	@Autowired
	private ViewConverter viewConverter;

	@Autowired
	private UserStore userStore;

	@Bean
	protected LookupStorableConverter lookupStorableConverter() {
		return new LookupStorableConverter();
	}

	@Bean
	protected DataViewStore<Lookup> baseLookupStore() {
		return new DataViewStore<Lookup>(systemDataView(), lookupStorableConverter());
	}

	@Bean
	public CachingStore<Lookup> cachedLookupStore() {
		return new CachingStore<Lookup>(baseLookupStore());
	}

	@Bean
	public LookupStore lookupStore() {
		return new DataViewLookupStore(cachedLookupStore());
	}

	@Bean
	@Scope(PROTOTYPE)
	public DataDefinitionLogic dataDefinitionLogic() {
		return new DataDefinitionLogic(systemDataView());
	}

	@Bean
	@Scope(PROTOTYPE)
	public LookupLogic lookupLogic() {
		return new LookupLogic(lookupStore(), userStore.getUser(), systemDataView());
	}

	@Bean
	@Scope(PROTOTYPE)
	public SecurityLogic securityLogic() {
		return new SecurityLogic(systemDataView(), viewConverter, filterStore, userStore.getUser());
	}

	@Bean
	@Scope(PROTOTYPE)
	public SoapAuthenticationLogicBuilder soapAuthenticationLogicBuilder() {
		return new SoapAuthenticationLogicBuilder( //
				soapAuthenticationService, //
				privilegeContextFactory, //
				systemDataView(), //
				userStore);
	}

	@Bean
	@Scope(PROTOTYPE)
	public SystemDataAccessLogicBuilder systemDataAccessLogicBuilder() {
		return new SystemDataAccessLogicBuilder( //
				systemDataView(), //
				lookupStore(), //
				systemDataView(), //
				userStore.getUser(), //
				systemLockCardManager);
	}

	@Bean
	@Qualifier("system")
	public DBDataView systemDataView() {
		return new DBDataView(dbDriver);
	}

	@Bean
	@Scope(PROTOTYPE)
	public ViewLogic viewLogic() {
		return new ViewLogic(systemDataView(), viewConverter, userStore.getUser());
	}

}
