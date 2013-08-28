package org.cmdbuild.spring.configuration;

import java.util.Arrays;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.CasAuthenticator;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.HeaderAuthenticator;
import org.cmdbuild.auth.LdapAuthenticator;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.auth.SoapDatabaseAuthenticator;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.UserTypeStore;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.auth.context.DefaultPrivilegeContextFactory;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.logic.auth.DefaultAuthenticationLogicBuilder;
import org.cmdbuild.privileges.DBGroupFetcher;
import org.cmdbuild.privileges.fetchers.factories.CMClassPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.FilterPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.ViewPrivilegeFetcherFactory;
import org.cmdbuild.services.soap.security.SoapConfiguration;
import org.cmdbuild.services.soap.security.SoapPasswordAuthenticator;
import org.cmdbuild.services.soap.security.SoapUserFetcher;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Authentication {

	@Autowired
	private AuthProperties authProperties;

	@Autowired
	private SoapConfiguration soapConfiguration;

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	private UserTypeStore userTypeStore;

	@Autowired
	private UserStore userStore;

	@Autowired
	private ViewConverter viewConverter;

	@Autowired
	private PrivilegeContextFactory privilegeContextFactory;

	@Bean
	@Qualifier("default")
	protected LegacyDBAuthenticator dbAuthenticator() {
		return new LegacyDBAuthenticator(systemDataView);
	}

	@Bean
	@Qualifier("soap")
	protected SoapDatabaseAuthenticator soapDatabaseAuthenticator() {
		return new SoapDatabaseAuthenticator(systemDataView);
	}

	@Bean
	@Qualifier("soap")
	protected SoapUserFetcher soapUserFetcher() {
		return new SoapUserFetcher(systemDataView, userTypeStore);
	}

	@Bean
	protected SoapPasswordAuthenticator soapPasswordAuthenticator() {
		return new SoapPasswordAuthenticator();
	}

	@Bean
	protected CasAuthenticator casAuthenticator() {
		return new CasAuthenticator(authProperties);
	}

	@Bean
	protected HeaderAuthenticator headerAuthenticator() {
		return new HeaderAuthenticator(authProperties);
	}

	@Bean
	protected LdapAuthenticator ldapAuthenticator() {
		return new LdapAuthenticator(authProperties);
	}

	@Bean
	@Scope("prototype")
	public DBGroupFetcher dbGroupFetcher() {
		return new DBGroupFetcher(systemDataView, Arrays.asList( //
				new CMClassPrivilegeFetcherFactory(systemDataView), //
				new ViewPrivilegeFetcherFactory(systemDataView, viewConverter), //
				new FilterPrivilegeFetcherFactory(systemDataView, userStore.getUser())));
	}

	@Bean
	@Qualifier("default")
	public AuthenticationService defaultAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(authProperties,
				systemDataView);
		authenticationService.setPasswordAuthenticators(dbAuthenticator(), ldapAuthenticator());
		authenticationService.setClientRequestAuthenticators(headerAuthenticator(), casAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		authenticationService.setUserStore(userStore);
		return authenticationService;
	}

	@Bean
	@Qualifier("soap")
	public AuthenticationService soapAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(soapConfiguration,
				systemDataView);
		authenticationService.setPasswordAuthenticators(soapPasswordAuthenticator());
		authenticationService.setUserFetchers(soapDatabaseAuthenticator(), soapUserFetcher());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		authenticationService.setUserStore(userStore);
		return authenticationService;
	}

	@Bean
	public PrivilegeContextFactory privilegeContextFactory() {
		return new DefaultPrivilegeContextFactory();
	}

	@Bean
	@Scope("prototype")
	public DefaultAuthenticationLogicBuilder defaultAuthenticationLogicBuilder() {
		return new DefaultAuthenticationLogicBuilder( //
				defaultAuthenticationService(), //
				privilegeContextFactory, //
				systemDataView, //
				userStore);
	}

}
