package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.DEFAULT;
import static org.cmdbuild.spring.util.Constants.PROTOTYPE;
import static org.cmdbuild.spring.util.Constants.SOAP;

import java.util.Arrays;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.CasAuthenticator;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.HeaderAuthenticator;
import org.cmdbuild.auth.LdapAuthenticator;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.auth.NotSystemUserFetcher;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.logic.auth.DefaultAuthenticationLogicBuilder;
import org.cmdbuild.logic.auth.DefaultGroupsLogic;
import org.cmdbuild.logic.auth.GroupsLogic;
import org.cmdbuild.logic.auth.RestAuthenticationLogicBuilder;
import org.cmdbuild.logic.auth.SoapAuthenticationLogicBuilder;
import org.cmdbuild.logic.auth.TransactionalGroupsLogic;
import org.cmdbuild.privileges.DBGroupFetcher;
import org.cmdbuild.privileges.fetchers.factories.CMClassPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.CustomPagePrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.FilterPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.ViewPrivilegeFetcherFactory;
import org.cmdbuild.services.soap.security.SoapConfiguration;
import org.cmdbuild.services.soap.security.SoapPasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Authentication {

	@Autowired
	private AuthenticationStore authenticationStore;

	@Autowired
	private CustomPages customPages;

	@Autowired
	private Data data;

	@Autowired
	private Filter filter;

	@Autowired
	private PrivilegeManagement privilegeManagement;

	@Autowired
	private Properties properties;

	@Autowired
	private SoapConfiguration soapConfiguration;

	@Autowired
	private UserStore userStore;

	@Autowired
	private View view;

	@Bean
	@Qualifier(DEFAULT)
	protected LegacyDBAuthenticator dbAuthenticator() {
		return new LegacyDBAuthenticator(data.systemDataView());
	}

	@Bean
	@Qualifier(SOAP)
	protected NotSystemUserFetcher notSystemUserFetcher() {
		return new NotSystemUserFetcher(data.systemDataView(), authenticationStore);
	}

	@Bean
	protected SoapPasswordAuthenticator soapPasswordAuthenticator() {
		return new SoapPasswordAuthenticator();
	}

	@Bean
	protected CasAuthenticator casAuthenticator() {
		return new CasAuthenticator(properties.authConf());
	}

	@Bean
	protected HeaderAuthenticator headerAuthenticator() {
		return new HeaderAuthenticator(properties.authConf());
	}

	@Bean
	protected LdapAuthenticator ldapAuthenticator() {
		return new LdapAuthenticator(properties.authConf());
	}

	@Bean
	@Scope(PROTOTYPE)
	public DBGroupFetcher dbGroupFetcher() {
		return new DBGroupFetcher(data.systemDataView(), Arrays.asList(
				new CMClassPrivilegeFetcherFactory(data.systemDataView()),
				new ViewPrivilegeFetcherFactory(data.systemDataView(), view.viewConverter()),
				new FilterPrivilegeFetcherFactory(data.systemDataView(), filter.dataViewFilterStore()),
				new CustomPagePrivilegeFetcherFactory(data.systemDataView(), customPages.defaultCustomPagesLogic())));
	}

	@Bean
	@Qualifier(DEFAULT)
	public AuthenticationService defaultAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(
				properties.authConf(), data.systemDataView());
		authenticationService.setPasswordAuthenticators(dbAuthenticator(), ldapAuthenticator());
		authenticationService.setClientRequestAuthenticators(headerAuthenticator(), casAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		authenticationService.setUserStore(userStore);
		return authenticationService;
	}

	@Bean
	@Qualifier(SOAP)
	public AuthenticationService soapAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(soapConfiguration,
				data.systemDataView());
		authenticationService.setPasswordAuthenticators(soapPasswordAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator(), notSystemUserFetcher());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		authenticationService.setUserStore(userStore);
		return authenticationService;
	}

	@Bean
	public AuthenticationService restAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(
				properties.authConf(), data.systemDataView());
		authenticationService.setPasswordAuthenticators(dbAuthenticator(), ldapAuthenticator());
		authenticationService.setClientRequestAuthenticators(headerAuthenticator(), casAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator(), notSystemUserFetcher());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		authenticationService.setUserStore(userStore);
		return authenticationService;
	}

	@Bean
	@Scope(PROTOTYPE)
	public DefaultAuthenticationLogicBuilder defaultAuthenticationLogicBuilder() {
		return new DefaultAuthenticationLogicBuilder( //
				defaultAuthenticationService(), //
				privilegeManagement.privilegeContextFactory(), //
				data.systemDataView());
	}

	@Bean
	@Scope(PROTOTYPE)
	@Qualifier(SOAP)
	public SoapAuthenticationLogicBuilder soapAuthenticationLogicBuilder() {
		return new SoapAuthenticationLogicBuilder( //
				soapAuthenticationService(), //
				privilegeManagement.privilegeContextFactory(), //
				data.systemDataView());
	}

	@Bean
	@Scope(PROTOTYPE)
	public RestAuthenticationLogicBuilder restAuthenticationLogicBuilder() {
		return new RestAuthenticationLogicBuilder( //
				restAuthenticationService(), //
				privilegeManagement.privilegeContextFactory(), //
				data.systemDataView());
	}

	@Bean
	public GroupsLogic groupsLogic() {
		return new TransactionalGroupsLogic(new DefaultGroupsLogic(defaultAuthenticationService(),
				data.systemDataView(), userStore));
	}

}
