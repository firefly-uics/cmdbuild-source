package unit.auth;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;

import java.util.Set;
import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.cmdbuild.services.auth.AuthInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@Deprecated
@RunWith(value = Parameterized.class)
public class AuthInfoTest {

	private static final String USER = "pluto";
	private static final String ANOTHER_USER = "scrooge";
	private static final String SHARK_USER = "minnie";
	private static final String SERVICE_USER_1 = "goofy";
	private static final String SERVICE_USER_2 = "mickey";
	private static final String SERVICE_USER_3 = "donald";

	private static final String ROLE = "clubhouse";

	private static final String DOMAIN_2LV = "@clubhouse.net";
	private static final String DOMAIN_3LV = "@moneybin.duckburg.net";

	private static final String HASH = "#";
	private static final String AT = "@";

	private final Context context;

	private AuthInfo authInfo;

	public AuthInfoTest(final Context context) {
		this.context = context;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays
				.asList(new Object[][] {
						// shark user
						{ new Context(SHARK_USER, SHARK_USER, SHARK_USER, true, StringUtils.EMPTY, true, false) },
						// user + group
						{ new Context(USER, USER, USER, false, StringUtils.EMPTY, true, false) },
						{ new Context(USER + AT + ROLE, USER, USER, false, ROLE, true, false) },
						// email + group
						{ new Context(USER + DOMAIN_2LV, USER + DOMAIN_2LV, USER + DOMAIN_2LV, false,
								StringUtils.EMPTY, true, false) },
						{ new Context(USER + DOMAIN_3LV, USER + DOMAIN_3LV, USER + DOMAIN_3LV, false,
								StringUtils.EMPTY, true, false) },
						{ new Context(USER + DOMAIN_2LV + AT + ROLE, USER + DOMAIN_2LV, USER + DOMAIN_2LV, false, ROLE,
								true, false) },
						{ new Context(USER + DOMAIN_3LV + AT + ROLE, USER + DOMAIN_3LV, USER + DOMAIN_3LV, false, ROLE,
								true, false) },
						// service user + user + group
						{ new Context(SERVICE_USER_1 + HASH + USER, USER, SERVICE_USER_1, false, StringUtils.EMPTY,
								true, true) },
						{ new Context(ANOTHER_USER + HASH + USER, USER, ANOTHER_USER, false, StringUtils.EMPTY, false,
								true) },
						{ new Context(SERVICE_USER_1 + HASH + USER + AT + ROLE, USER, SERVICE_USER_1, false, ROLE,
								true, true) },
						{ new Context(ANOTHER_USER + HASH + USER + AT + ROLE, USER, ANOTHER_USER, false, ROLE, false,
								true) },
						// service email + user + group
						{ new Context(SERVICE_USER_1 + DOMAIN_2LV + HASH + USER, USER, SERVICE_USER_1 + DOMAIN_2LV,
								false, StringUtils.EMPTY, true, true) },
						{ new Context(SERVICE_USER_1 + DOMAIN_3LV + HASH + USER, USER, SERVICE_USER_1 + DOMAIN_3LV,
								false, StringUtils.EMPTY, true, true) },
						{ new Context(ANOTHER_USER + DOMAIN_2LV + HASH + USER, USER, ANOTHER_USER + DOMAIN_2LV, false,
								StringUtils.EMPTY, false, true) },
						{ new Context(ANOTHER_USER + DOMAIN_3LV + HASH + USER, USER, ANOTHER_USER + DOMAIN_3LV, false,
								StringUtils.EMPTY, false, true) },
						{ new Context(SERVICE_USER_1 + DOMAIN_2LV + HASH + USER + AT + ROLE, USER, SERVICE_USER_1
								+ DOMAIN_2LV, false, ROLE, true, true) },
						{ new Context(SERVICE_USER_1 + DOMAIN_3LV + HASH + USER + AT + ROLE, USER, SERVICE_USER_1
								+ DOMAIN_3LV, false, ROLE, true, true) },
						{ new Context(ANOTHER_USER + DOMAIN_2LV + HASH + USER + AT + ROLE, USER, ANOTHER_USER
								+ DOMAIN_2LV, false, ROLE, false, true) },
						{ new Context(ANOTHER_USER + DOMAIN_3LV + HASH + USER + AT + ROLE, USER, ANOTHER_USER
								+ DOMAIN_3LV, false, ROLE, false, true) },
						// service user + e-mail + group
						{ new Context(SERVICE_USER_1 + HASH + USER + DOMAIN_2LV, USER + DOMAIN_2LV, SERVICE_USER_1,
								false, StringUtils.EMPTY, true, true) },
						{ new Context(SERVICE_USER_1 + HASH + USER + DOMAIN_3LV, USER + DOMAIN_3LV, SERVICE_USER_1,
								false, StringUtils.EMPTY, true, true) },
						{ new Context(ANOTHER_USER + HASH + USER + DOMAIN_2LV, USER + DOMAIN_2LV, ANOTHER_USER, false,
								StringUtils.EMPTY, false, true) },
						{ new Context(ANOTHER_USER + HASH + USER + DOMAIN_3LV, USER + DOMAIN_3LV, ANOTHER_USER, false,
								StringUtils.EMPTY, false, true) },
						{ new Context(SERVICE_USER_1 + HASH + USER + DOMAIN_2LV + AT + ROLE, USER + DOMAIN_2LV,
								SERVICE_USER_1, false, ROLE, true, true) },
						{ new Context(SERVICE_USER_1 + HASH + USER + DOMAIN_3LV + AT + ROLE, USER + DOMAIN_3LV,
								SERVICE_USER_1, false, ROLE, true, true) },
						{ new Context(ANOTHER_USER + HASH + USER + DOMAIN_2LV + AT + ROLE, USER + DOMAIN_2LV,
								ANOTHER_USER, false, ROLE, false, true) },
						{ new Context(ANOTHER_USER + HASH + USER + DOMAIN_3LV + AT + ROLE, USER + DOMAIN_3LV,
								ANOTHER_USER, false, ROLE, false, true) },
						// service email + e-mail + group
						{ new Context(SERVICE_USER_1 + DOMAIN_2LV + HASH + USER + DOMAIN_2LV, USER + DOMAIN_2LV,
								SERVICE_USER_1 + DOMAIN_2LV, false, StringUtils.EMPTY, true, true) },
						{ new Context(SERVICE_USER_1 + DOMAIN_3LV + HASH + USER + DOMAIN_2LV, USER + DOMAIN_2LV,
								SERVICE_USER_1 + DOMAIN_3LV, false, StringUtils.EMPTY, true, true) },
						{ new Context(SERVICE_USER_1 + DOMAIN_2LV + HASH + USER + DOMAIN_3LV, USER + DOMAIN_3LV,
								SERVICE_USER_1 + DOMAIN_2LV, false, StringUtils.EMPTY, true, true) },
						{ new Context(SERVICE_USER_1 + DOMAIN_3LV + HASH + USER + DOMAIN_3LV, USER + DOMAIN_3LV,
								SERVICE_USER_1 + DOMAIN_3LV, false, StringUtils.EMPTY, true, true) },
						{ new Context(ANOTHER_USER + DOMAIN_2LV + HASH + USER + DOMAIN_2LV, USER + DOMAIN_2LV,
								ANOTHER_USER + DOMAIN_2LV, false, StringUtils.EMPTY, false, true) },
						{ new Context(ANOTHER_USER + DOMAIN_3LV + HASH + USER + DOMAIN_2LV, USER + DOMAIN_2LV,
								ANOTHER_USER + DOMAIN_3LV, false, StringUtils.EMPTY, false, true) },
						{ new Context(ANOTHER_USER + DOMAIN_2LV + HASH + USER + DOMAIN_3LV, USER + DOMAIN_3LV,
								ANOTHER_USER + DOMAIN_2LV, false, StringUtils.EMPTY, false, true) },
						{ new Context(ANOTHER_USER + DOMAIN_3LV + HASH + USER + DOMAIN_3LV, USER + DOMAIN_3LV,
								ANOTHER_USER + DOMAIN_3LV, false, StringUtils.EMPTY, false, true) },
						{ new Context(SERVICE_USER_1 + DOMAIN_2LV + HASH + USER + DOMAIN_2LV + AT + ROLE, USER
								+ DOMAIN_2LV, SERVICE_USER_1 + DOMAIN_2LV, false, ROLE, true, true) },
						{ new Context(SERVICE_USER_1 + DOMAIN_3LV + HASH + USER + DOMAIN_2LV + AT + ROLE, USER
								+ DOMAIN_2LV, SERVICE_USER_1 + DOMAIN_3LV, false, ROLE, true, true) },
						{ new Context(SERVICE_USER_1 + DOMAIN_2LV + HASH + USER + DOMAIN_3LV + AT + ROLE, USER
								+ DOMAIN_3LV, SERVICE_USER_1 + DOMAIN_2LV, false, ROLE, true, true) },
						{ new Context(SERVICE_USER_1 + DOMAIN_3LV + HASH + USER + DOMAIN_3LV + AT + ROLE, USER
								+ DOMAIN_3LV, SERVICE_USER_1 + DOMAIN_3LV, false, ROLE, true, true) },
						{ new Context(ANOTHER_USER + DOMAIN_2LV + HASH + USER + DOMAIN_2LV + AT + ROLE, USER
								+ DOMAIN_2LV, ANOTHER_USER + DOMAIN_2LV, false, ROLE, false, true) },
						{ new Context(ANOTHER_USER + DOMAIN_3LV + HASH + USER + DOMAIN_2LV + AT + ROLE, USER
								+ DOMAIN_2LV, ANOTHER_USER + DOMAIN_3LV, false, ROLE, false, true) },
						{ new Context(ANOTHER_USER + DOMAIN_2LV + HASH + USER + DOMAIN_3LV + AT + ROLE, USER
								+ DOMAIN_3LV, ANOTHER_USER + DOMAIN_2LV, false, ROLE, false, true) },
						{ new Context(ANOTHER_USER + DOMAIN_3LV + HASH + USER + DOMAIN_3LV + AT + ROLE, USER
								+ DOMAIN_3LV, ANOTHER_USER + DOMAIN_3LV, false, ROLE, false, true) },
				//
				});

	}

	@Before
	public void setUp() {
		authInfo = new AuthInfo(context.authData) {
			@Override
			protected Set<String> getServiceUsers() {
				return Sets.newHashSet(new String[] { SERVICE_USER_1, SERVICE_USER_2, SERVICE_USER_3, //
						SERVICE_USER_1 + DOMAIN_2LV, SERVICE_USER_2 + DOMAIN_2LV, SERVICE_USER_3 + DOMAIN_2LV,//
						SERVICE_USER_1 + DOMAIN_3LV, SERVICE_USER_2 + DOMAIN_3LV, SERVICE_USER_3 + DOMAIN_3LV });
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
		Assert.assertEquals(context.toString(), context.username, authInfo.getUsername());
	}

	@Test
	public void testGetUsernameForAutentication() {
		if (context.isValidServiceUser) {
			Assert.assertEquals(context.toString(), context.usernameForAutentication, authInfo
					.getUsernameForAuthentication());
		} else {
			try {
				authInfo.getUsernameForAuthentication();
				Assert.fail("expected exception for " + context.toString());
			} catch (final Exception e) {
				// ok
			}
		}

	}

	@Test
	public void testIsSharkUser() {
		Assert.assertEquals(context.toString(), context.isSharkUser, authInfo.isSharkUser());
	}

	@Test
	public void testGetRole() {
		Assert.assertEquals(context.toString(), context.role, authInfo.getRole());
	}

	@Test
	public void testHasServiceUser() {
		Assert.assertEquals(context.toString(), context.hasServiceUser, authInfo.hasServiceUser());
	}

	private static class Context {

		public final String authData;
		public final String username;
		public final String usernameForAutentication;
		public final boolean isSharkUser;
		public final String role;
		public final boolean isValidServiceUser;
		public final boolean hasServiceUser;

		public Context(final String authData, final String username, final String usernameForAutentication,
				final boolean isSharkUser, final String role, final boolean isValidServiceUser, final boolean hasServiceUser) {
			this.authData = authData;
			this.username = username;
			this.usernameForAutentication = usernameForAutentication;
			this.isSharkUser = isSharkUser;
			this.role = role;
			this.isValidServiceUser = isValidServiceUser;
			this.hasServiceUser = hasServiceUser;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append("authData", authData).append("username", username).append(
					"usernameForAutentication", usernameForAutentication).append("isSharkUser", isSharkUser).append("role",
					role).append("isValidServiceUser", isValidServiceUser).append("hasServiceUser", hasServiceUser)
					.toString();
		}

	}

}
