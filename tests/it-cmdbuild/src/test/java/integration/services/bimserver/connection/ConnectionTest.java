package integration.services.bimserver.connection;

import static org.junit.Assert.assertTrue;

import org.cmdbuild.bim.service.bimserver.BimserverClient;
import org.cmdbuild.bim.service.bimserver.BimserverConfiguration;
import org.cmdbuild.bim.service.bimserver.DefaultBimserverClient;
import org.cmdbuild.bim.service.bimserver.SmartBimserverClient;
import org.junit.Test;

public class ConnectionTest {

	@Test
	public void ifTheConfigurationIsEmptyClientIsNotConnected() throws Exception {

		// given
		final BimserverConfiguration conf = new BimserverConfiguration() {

			@Override
			public boolean isEnabled() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String getUsername() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getUrl() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getPassword() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void disable() {
				// TODO Auto-generated method stub

			}

			@Override
			public void addListener(final ChangeListener listener) {
				// TODO Auto-generated method stub

			}
		};

		// when
		final BimserverClient client = new SmartBimserverClient(new DefaultBimserverClient(conf));

		// then
		assertTrue(!client.isConnected());
	}

	@Test
	public void ifBimserverIsDownClientIsNotConnected() throws Exception {

		// given
		final BimserverConfiguration conf = new BimserverConfiguration() {

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public String getUsername() {
				return "username";
			}

			@Override
			public String getUrl() {
				return "the_url";
			}

			@Override
			public String getPassword() {
				return "password";
			}

			@Override
			public void disable() {
			}

			@Override
			public void addListener(final ChangeListener listener) {
			}
		};

		// when
		final BimserverClient client = new SmartBimserverClient(new DefaultBimserverClient(conf));

		// then
		assertTrue(!client.isConnected());
	}

	@Test
	public void ifBimserverIsRunningConnectionIsSuccessful() throws Exception {

		// given
		final BimserverConfiguration conf = new BimserverConfiguration() {

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public String getUsername() {
				return "admin@tecnoteca.com";
			}

			@Override
			public String getUrl() {
				return "http://localhost:10080";
			}

			@Override
			public String getPassword() {
				return "admin";
			}

			@Override
			public void disable() {
				// TODO Auto-generated method stub

			}

			@Override
			public void addListener(final ChangeListener listener) {
				// TODO Auto-generated method stub

			}
		};

		// when
		final BimserverClient client = new SmartBimserverClient(new DefaultBimserverClient(conf));

		// then
		assertTrue(client.isConnected());
	}

	@Test
	public void connectAndDisconnectChangingConfiguration() throws Exception {

		// given
		final BimserverConfiguration conf = new BimserverConfiguration() {

			private ChangeListener listener;

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public String getUsername() {
				return "admin@tecnoteca.com";
			}

			@Override
			public String getUrl() {
				return "http://localhost:10080";
			}

			@Override
			public String getPassword() {
				return "admin";
			}

			@Override
			public void disable() {
				notifyListener();
			}

			private void notifyListener() {
				listener.configurationChanged();
			}

			@Override
			public void addListener(final ChangeListener listener) {
				this.listener = listener;

			}
		};
		final BimserverClient client = new SmartBimserverClient(new DefaultBimserverClient(conf));
		assertTrue(client.isConnected());

		// when
		conf.disable();

		// then
		assertTrue(!client.isConnected());
	}

}
