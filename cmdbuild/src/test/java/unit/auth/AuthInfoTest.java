package unit.auth;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.services.auth.AuthInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class AuthInfoTest {

	private static final String USER = "pluto";
	private static final String SHARK_USER = "minnie";
	private static final String SERVICE_USER_1 = "goofy";
	private static final String SERVICE_USER_2 = "mickey";
	private static final String SERVICE_USER_3 = "donald";

	private static final String DEFAULT_GROUP = "clubhouse";

	private static final String HASH = AuthInfo.USERS_SEPARATOR;
	private static final String AT = AuthInfo.GROUP_SEPARATOR;

	private final Context context;

	private AuthInfo authInfo;

	public AuthInfoTest(final Context context) {
		this.context = context;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ new Context(USER, USER, USER, false, StringUtils.EMPTY) },
				{ new Context(SHARK_USER, SHARK_USER, SHARK_USER, true, StringUtils.EMPTY) },
				{ new Context(USER + AT + DEFAULT_GROUP, USER, USER, false, DEFAULT_GROUP) },
				{ new Context(SERVICE_USER_1 + HASH + USER, USER, SERVICE_USER_1, false, StringUtils.EMPTY) },
				{ new Context(SERVICE_USER_1 + HASH + USER + AT + DEFAULT_GROUP, USER, SERVICE_USER_1, false,
						DEFAULT_GROUP) }, });
	}

	@Before
	public void setUp() {
		authInfo = new AuthInfo(context.authData) {
			@Override
			protected String[] getServiceUsers() {
				return new String[] { SERVICE_USER_1, SERVICE_USER_2, SERVICE_USER_3 };
			}

			@Override
			protected String getSharkWSUser() {
				return SHARK_USER;
			}
		};
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAuthDataWithNullString() {
		new AuthInfo(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAuthDataWithEmptyString() {
		new AuthInfo(StringUtils.EMPTY);
	}

	@Test
	public void testGetUsername() {
		Assert.assertEquals(context.username, authInfo.getUsername());
	}

	@Test
	public void testGetUsernameForAutentication() {
		Assert.assertEquals(context.usernameForAutentication, authInfo.getUsernameForAuthentication());
	}

	@Test
	public void testIsSharkUser() {
		Assert.assertEquals(context.isSharkUser, authInfo.isSharkUser());
	}

	@Test
	public void testGetRole() {
		Assert.assertEquals(context.role, authInfo.getRole());
	}

}

class Context {

	public final String authData;
	public final String username;
	public final String usernameForAutentication;
	public final boolean isSharkUser;
	public final String role;

	public Context(final String authData, final String username, final String usernameForAutentication,
			final boolean isSharkUser, final String role) {
		this.authData = authData;
		this.username = username;
		this.usernameForAutentication = usernameForAutentication;
		this.isSharkUser = isSharkUser;
		this.role = role;
	}

}
