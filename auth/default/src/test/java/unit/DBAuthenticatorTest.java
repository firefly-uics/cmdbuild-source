package unit;

import static org.mockito.Mockito.mock;

import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.dao.view.CMDataView;
import org.junit.Test;

public class DBAuthenticatorTest {

	private final CMDataView view = mock(CMDataView.class);

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void viewCannotBeNull() {
		@SuppressWarnings("unused")
		final LegacyDBAuthenticator authenticator = new LegacyDBAuthenticator(null);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void passwordHandlerIfProvidedCannotBeNull() {
		@SuppressWarnings("unused")
		final LegacyDBAuthenticator authenticator = new LegacyDBAuthenticator(view, null);
	}
}
