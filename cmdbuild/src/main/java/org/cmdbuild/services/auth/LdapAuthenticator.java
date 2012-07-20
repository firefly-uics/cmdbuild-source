package org.cmdbuild.services.auth;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logger.Log;

public class LdapAuthenticator implements Authenticator {

	private static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

	public LdapAuthenticator() {
		if (!AuthProperties.getInstance().isLdapConfigured()) {
			throw AuthExceptionType.AUTH_NOT_CONFIGURED.createException();
		}
	}

	@Override
	public UserContext headerAuth(final HttpServletRequest request) {
		return null;
	}

	@Override
	public UserContext jsonRpcAuth(final String username, final String unencryptedPassword) {
		return authenticateInLdap(username, unencryptedPassword);
	}

	private UserContext authenticateInLdap(final String username, final String unencryptedPassword) {
		try {
			final DirContext ctx = initialize();
			final String ldapUser = getUser(ctx, username);
			if ((ldapUser != null) && bind(ctx, ldapUser, unencryptedPassword)) {
				return new AuthInfo(username).systemAuth();
			}
		} catch (final NamingException e) {
		}
		Log.AUTH.warn(String.format("Cannot authenticate user '%s' on LDAP", username));
		return null;
	}

	@Override
	public boolean wsAuth(final WSPasswordCallback pwcb) {
		final String identifier = pwcb.getIdentifier();
		final AuthInfo authInfo = new AuthInfo(identifier);
		final String username = authInfo.getUsernameForAuthentication();
		final String unencryptedPassword = pwcb.getPassword();
		return (unencryptedPassword != null && authenticateInLdap(username, unencryptedPassword) != null);
	}

	private DirContext initialize() throws NamingException {
		final Properties env = new Properties();
		final AuthProperties props = AuthProperties.getInstance();
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, props.getLdapUrl());
		env.put(Context.REFERRAL, "follow");
		try {
			final DirContext ctx = new InitialDirContext(env);
			setOriginalAuthentication(ctx);
			return ctx;
		} catch (final NamingException e) {
			Log.AUTH.warn("Cannot set LDAP properties", e);
			throw e;
		}
	}

	private String getUser(final DirContext ctx, final String userToFind) {
		final SearchControls sc = new SearchControls();
		sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String usertobind = null;
		final AuthProperties props = AuthProperties.getInstance();
		final String searchFilter = generateSearchFilter(userToFind);
		try {
			final NamingEnumeration<SearchResult> results = ctx.search(props.getLdapBaseDN(), searchFilter, sc);
			if (results.hasMore()) {
				final SearchResult sr = results.next();
				usertobind = sr.getNameInNamespace();
			}
		} catch (final NamingException e) {
			Log.AUTH.debug("LDAP error", e);
			assert usertobind == null;
		}
		return usertobind;
	}

	private String generateSearchFilter(final String userToFind) {
		final AuthProperties props = AuthProperties.getInstance();
		final String searchFilter = props.getLdapSearchFilter();
		String searchQuery;
		if (searchFilter != null) {
			searchQuery = String.format("(&%s(%s=%s))", props.getLdapSearchFilter(), props.getLdapBindAttribute(),
					userToFind);
		} else {
			searchQuery = String.format("(%s=%s)", props.getLdapBindAttribute(), userToFind);
		}
		Log.AUTH.debug("LDAP generated search query: " + searchQuery.toString());
		return searchQuery.toString();
	}

	private boolean bind(final DirContext ctx, final String username, final String password) throws NamingException {
		boolean validate = false;
		try {
			Log.AUTH.debug("Setting simple bind to authenticate");
			ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
			Log.AUTH.debug("Binding with username " + username);
			ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, username);
			Log.AUTH.trace("Binding with password " + password);
			ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
			ctx.getAttributes(StringUtils.EMPTY, null);
			validate = true;
		} catch (final NamingException e) {
			Log.AUTH.info(String.format("Cannot execute LDAP authentication for user %s", username));
			Log.AUTH.debug(e);
			Log.AUTH.debug("Restoring defaults");
			setOriginalAuthentication(ctx);
			validate = false;
		} finally {
			// Terminate context
			ctx.close();
		}
		return validate;
	}

	private void setOriginalAuthentication(final DirContext ctx) throws NamingException {
		final AuthProperties props = AuthProperties.getInstance();
		ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, props.getLdapAuthenticationMethod());
		if (!StringUtils.EMPTY.equals(props.getLdapPrincipal()) && props.getLdapPrincipal() != null) {
			ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, props.getLdapPrincipal());
		}
		if (!StringUtils.EMPTY.equals(props.getLdapPrincipalCredentials())
				&& props.getLdapPrincipalCredentials() != null) {
			ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, props.getLdapPrincipalCredentials());
		}
	}

	@Override
	public boolean canChangePassword() {
		return false;
	}

	@Override
	public void changePassword(final String username, final String oldPassword, final String newPassword) {
		throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
	}

	@Override
	public boolean allowsPasswordLogin() {
		return true;
	}
}
