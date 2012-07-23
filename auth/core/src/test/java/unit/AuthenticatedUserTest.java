package unit;

import org.cmdbuild.auth.user.*;
import org.junit.Test;
import static org.cmdbuild.auth.user.AnonymousUser.ANONYMOUS_USER;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class AuthenticatedUserTest {

	@Test
	public void nullUserCreatesAnAnonymousUser() {
		assertThat(AuthenticatedUser.newInstance(null), is(ANONYMOUS_USER));
	}

	@Test
	public void notNullUserWrapsIt() {
		final CMUser user = new UserImpl();
		assertThat(AuthenticatedUser.newInstance(user), instanceOf(AuthenticatedUserWrapper.class));
	}
}
