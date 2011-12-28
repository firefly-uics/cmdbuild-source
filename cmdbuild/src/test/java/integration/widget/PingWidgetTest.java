package integration.widget;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import org.cmdbuild.elements.widget.Ping;
import org.junit.Ignore;
import org.junit.Test;

public class PingWidgetTest {

	private static final String LOCALHOST;
	private static final String LOCALIP;

	static {
		InetAddress local;
		try {
			local = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			local = null;
		}
		if (local != null) {
			LOCALHOST = local.getHostName();
			LOCALIP = local.getHostAddress();
		} else {
			LOCALHOST = "localhost";
			LOCALIP = "127.0.0.1";
		}
	}

	@Test
	public void pingsLocalNumericAddress() throws Exception {
		assertPingsAddress(LOCALIP);
	}

	@Test
	public void pingsLocalhost() throws Exception {
		assertPingsAddress(LOCALHOST);
	}

	@Test(timeout=3000, expected=TimeoutException.class)
	public void throwsOnTimeoutExpired() throws Exception {
		Ping pingWidget = new Ping() {
			protected long getMillisTimeout() {
				return 100L;
			}
		};
		pingWidget.setAddress(LOCALIP);
		pingWidget.setCount(10);
		pingWidget.executeAction(null, null);
	}

	@Ignore
	@Test
	public void addressIsConsideredATemplate() throws Exception {
		// Not developed with TDD, and not testable!
	}

	/*
	 * Utils
	 */

	@SuppressWarnings("unchecked")
	private void assertPingsAddress(final String address) throws Exception {
		String result = (String) newPingWidget(address, 1).executeAction(null, null);
		assertThat(result.toLowerCase(), allOf(containsString("byte"), containsString("ms"), containsString("ping")));
	}

	private Ping newPingWidget(final String address, int count) {
		Ping pingWidget = new Ping();
		pingWidget.setAddress(address);
		pingWidget.setCount(count);
		return pingWidget;
	}
}
