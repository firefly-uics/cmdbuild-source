package unit.api.fluent.ws;

import static org.mockito.Mockito.mock;

import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.ws.WsFluentApi;
import org.cmdbuild.services.soap.Private;
import org.junit.Before;

public abstract class AbstractWsFluentApiTest {

	private static final int DEFAULT_RANDOM_STRING_COUNT = 10;

	private Private proxy;
	private FluentApi api;

	@Before
	public void createApi() throws Exception {
		proxy = mock(Private.class);
		api = new WsFluentApi(proxy);
	}

	protected Private proxy() {
		return proxy;
	}

	protected FluentApi api() {
		return api;
	}

	protected static String randomString() {
		return randomString(DEFAULT_RANDOM_STRING_COUNT);
	}

	protected static String randomString(final int count) {
		return RandomStringUtils.random(count);
	}

}
