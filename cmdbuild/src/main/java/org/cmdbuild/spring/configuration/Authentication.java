package org.cmdbuild.spring.configuration;

import static java.util.Arrays.asList;
import static org.cmdbuild.spring.util.Constants.DEFAULT;
import static org.cmdbuild.spring.util.Constants.SOAP;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.CasAuthenticator;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.HeaderAuthenticator;
import org.cmdbuild.auth.LdapAuthenticator;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.auth.NotSystemUserFetcher;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.data.store.InMemoryStore;
import org.cmdbuild.data.store.session.DefaultSessionStore;
import org.cmdbuild.data.store.session.Session;
import org.cmdbuild.data.store.session.SessionStore;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.DefaultAuthenticationLogic;
import org.cmdbuild.logic.auth.DefaultGroupsLogic;
import org.cmdbuild.logic.auth.DefaultSessionLogic;
import org.cmdbuild.logic.auth.GroupsLogic;
import org.cmdbuild.logic.auth.RestSessionLogic;
import org.cmdbuild.logic.auth.SoapSessionLogic;
import org.cmdbuild.logic.auth.StandardSessionLogic;
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

	@Autowired
	private Web web;

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
	public DBGroupFetcher dbGroupFetcher() {
		return new DBGroupFetcher(data.systemDataView(),
				asList(new CMClassPrivilegeFetcherFactory(data.systemDataView()), new ViewPrivilegeFetcherFactory(
						data.systemDataView(), view.viewConverter()),
				new FilterPrivilegeFetcherFactory(data.systemDataView(), filter.dataViewFilterStore()),
				new CustomPagePrivilegeFetcherFactory(data.systemDataView(), customPages.defaultCustomPagesLogic())));
	}

	@Bean
	public AuthenticationService defaultAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(
				properties.authConf(), data.systemDataView());
		authenticationService.setPasswordAuthenticators(dbAuthenticator(), ldapAuthenticator());
		authenticationService.setClientRequestAuthenticators(headerAuthenticator(), casAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		return authenticationService;
	}

	@Bean
	protected AuthenticationService soapAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(soapConfiguration,
				data.systemDataView());
		authenticationService.setPasswordAuthenticators(soapPasswordAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator(), notSystemUserFetcher());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		return authenticationService;
	}

	@Bean
	protected AuthenticationService restAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(
				properties.authConf(), data.systemDataView());
		authenticationService.setPasswordAuthenticators(dbAuthenticator(), ldapAuthenticator());
		authenticationService.setClientRequestAuthenticators(headerAuthenticator(), casAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator(), notSystemUserFetcher());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		return authenticationService;
	}

	@Bean
	public StandardSessionLogic standardSessionLogic() {
		final DefaultAuthenticationLogic delegate = new DefaultAuthenticationLogic(defaultAuthenticationService(),
				privilegeManagement.privilegeContextFactory(), data.systemDataView());
		return new StandardSessionLogic(
				new DefaultSessionLogic(delegate, userStore, sessionStore(), web.simpleTokenGenerator()));
	}

	@Bean
	public SoapSessionLogic soapSessionLogic() {
		final AuthenticationLogic delegate = new DefaultAuthenticationLogic(soapAuthenticationService(),
				privilegeManagement.privilegeContextFactory(), data.systemDataView());
		return new SoapSessionLogic(
				new DefaultSessionLogic(delegate, userStore, sessionStore(), web.simpleTokenGenerator()));
	}

	@Bean
	public RestSessionLogic restSessionLogic() {
		final AuthenticationLogic delegate = new DefaultAuthenticationLogic(restAuthenticationService(),
				privilegeManagement.privilegeContextFactory(), data.systemDataView());
		return new RestSessionLogic(
				new DefaultSessionLogic(delegate, userStore, sessionStore(), web.simpleTokenGenerator()));
	}

	@Bean
	protected SessionStore sessionStore() {
		return new DefaultSessionStore(InMemoryStore.of(Session.class));
	}

	@Bean
	public GroupsLogic groupsLogic() {
		return new TransactionalGroupsLogic(
				new DefaultGroupsLogic(defaultAuthenticationService(), data.systemDataView(), userStore));
	}

}
