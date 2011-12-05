package unit;

import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.dao.view.CMDataView;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class DBAuthenticatorTest {

	private final CMDataView view = mock(CMDataView.class);

	@Test(expected=java.lang.IllegalArgumentException.class)
	public void viewCannotBeNull() {
		LegacyDBAuthenticator authenticator = new LegacyDBAuthenticator(null);
	}

	@Test(expected=java.lang.IllegalArgumentException.class)
	public void passwordHandlerIfProvidedCannotBeNull() {
		LegacyDBAuthenticator authenticator = new LegacyDBAuthenticator(view, null);
	}
}
