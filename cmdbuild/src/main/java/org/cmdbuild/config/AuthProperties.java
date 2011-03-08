package org.cmdbuild.config;

import java.util.Arrays;
import java.util.List;

import org.cmdbuild.services.Settings;

public class AuthProperties extends DefaultProperties {

	private static final long serialVersionUID = 1L;
	private static final String MODULE_NAME = "auth";
	private static final String SERVICE_USERS = "serviceusers";
	private static final String FORCE_WS_PASSWORD_DIGEST = "force.ws.password.digest";
	private static final String HEADER_ATTRIBUTE_NAME = "header.attribute.name";
	private static final String CAS_SERVER_URL = "cas.server.url";
	private static final String CAS_SERVICE_PARAM = "cas.service.param";
	private static final String CAS_TICKET_PARAM = "cas.ticket.param";
	private static final String LDAP_SERVER_ADDRESS = "ldap.server.address";
	private static final String LDAP_SERVER_PORT = "ldap.server.port";
	private static final String LDAP_USE_SSL = "ldap.use.ssl";
	private static final String LDAP_BASEDN = "ldap.basedn";
	private static final String LDAP_BIND_ATTRIBUTE = "ldap.bind.attribute";
	private static final String LDAP_SEARCH_FILTER = "ldap.search.filter";
	private static final String LDAP_AUTHENTICATION_METHOD = "ldap.search.auth.method";
	private static final String LDAP_AUTHENTICATION_PRINCIPAL = "ldap.search.auth.principal";
	private static final String LDAP_AUTHENTICATION_PASSWORD = "ldap.search.auth.password";
	private static final String AUTH_METHODS = "auth.methods";
	private static final String AUTO_LOGIN = "autologin";

	public AuthProperties() {
		super();
		setProperty(SERVICE_USERS, "");
		setProperty(FORCE_WS_PASSWORD_DIGEST, String.valueOf(true));
		setProperty(AUTH_METHODS, "DBAuthenticator");
		setProperty(HEADER_ATTRIBUTE_NAME, "username");
		setProperty(CAS_SERVER_URL, "");
		setProperty(LDAP_BASEDN, "");
		setProperty(LDAP_SERVER_ADDRESS, "");
		setProperty(LDAP_SERVER_PORT, "389");
		setProperty(LDAP_USE_SSL, String.valueOf(false));
		setProperty(LDAP_BIND_ATTRIBUTE, "");
		setProperty(LDAP_SEARCH_FILTER, "");
		setProperty(LDAP_AUTHENTICATION_METHOD, "");
		setProperty(LDAP_AUTHENTICATION_PRINCIPAL, "");
		setProperty(LDAP_AUTHENTICATION_PASSWORD, "");
	}

	public boolean isLdapConfigured() {
		return !("".equals(getLdapBindAttribute()) || "".equals(getLdapBaseDN()) || "".equals(getLdapServerAddress()));
	}

	public boolean isHeaderConfigured() {
		return !("".equals(getHeaderAttributeName()));
	}

	public boolean isCasConfigured() {
		return !("".equals(getCasServerUrl()));
	}

	public static AuthProperties getInstance() {
		return (AuthProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	public String[] getServiceUsers() {
		String commaSeparatedUsers = getProperty(SERVICE_USERS);
		if (commaSeparatedUsers.length() == 0)
			return new String[0];
		return commaSeparatedUsers.split(",");
	}

	public boolean getForceWSPasswordDigest() {
		return Boolean.parseBoolean(getProperty(FORCE_WS_PASSWORD_DIGEST));
	}

	public String getHeaderAttributeName() {
		return getProperty(HEADER_ATTRIBUTE_NAME);
	}

	public String getCasServerUrl() {
		return getProperty(CAS_SERVER_URL);
	}

	public String getCasTicketParam() {
		return getProperty(CAS_TICKET_PARAM, "ticket");
	}

	public String getCasServiceParam() {
		return getProperty(CAS_SERVICE_PARAM, "service");
	}

	public String getLdapUrl() {
		return String.format("%s://%s:%s", getLdapProtocol(), getLdapServerAddress(), getLdapServerPort());
	}

	private String getLdapServerAddress() {
		return getProperty(LDAP_SERVER_ADDRESS);
	}

	private String getLdapServerPort() {
		return getProperty(LDAP_SERVER_PORT);
	}

	private String getLdapProtocol() {
		return getLdapUseSsl() ? "ldaps" : "ldap";
	}

	private boolean getLdapUseSsl() {
		return Boolean.parseBoolean(getProperty(LDAP_USE_SSL));
	}

	public String getLdapBaseDN() {
		return getProperty(LDAP_BASEDN);
	}

	public String getLdapBindAttribute() {
		return getProperty(LDAP_BIND_ATTRIBUTE);
	}

	public String getLdapSearchFilter() {
		return getProperty(LDAP_SEARCH_FILTER);
	}

	public String getLdapAuthenticationMethod() {
		return getProperty(LDAP_AUTHENTICATION_METHOD);
	}

	public String getLdapPrincipal() {
		return getProperty(LDAP_AUTHENTICATION_PRINCIPAL);
	}

	public String getLdapPrincipalCredentials() {
		return getProperty(LDAP_AUTHENTICATION_PASSWORD);
	}

	public List<String> getAuthMethodNames() {
		String csvNames = getProperty(AUTH_METHODS);
		return Arrays.asList(csvNames.split(","));
	}

	public String getAutologin() {
		return getProperty(AUTO_LOGIN, "");
	}
}
