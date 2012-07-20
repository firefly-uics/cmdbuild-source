package unit.api.fluent.ws;

import static org.apache.commons.lang.RandomStringUtils.random;
import static org.mockito.Mockito.mock;

import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.services.soap.Relation;
import org.junit.Before;
import org.mockito.ArgumentCaptor;

public abstract class AbstractWsFluentApiTest {

	protected static final String CLASSNAME = "classname";
	protected static final String ANOTHER_CLASSNAME = randomString();

	protected static final String ATTRIBUTE_1 = "attribute_1";
	protected static final String ATTRIBUTE_2 = "attribute_2";
	protected static final String MISSING_ATTRIBUTE = "missing_attribute";

	protected static final String CODE_VALUE = randomString();
	protected static final String DESCRIPTION_VALUE = randomString();
	protected static final String ATTRIBUTE_1_VALUE = randomString();
	protected static final String ATTRIBUTE_2_VALUE = randomString();

	protected static final String DOMAIN_NAME = "domainname";

	protected static final int CARD_ID = 123;
	protected static final int ANOTHER_CARD_ID = 321;

	private static final int DEFAULT_RANDOM_STRING_COUNT = 10;

	private Private proxy;
	private FluentApi api;

	private final ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
	private final ArgumentCaptor<Relation> relationCaptor = ArgumentCaptor.forClass(Relation.class);
	private final ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

	@Before
	public void createProxyAndApi() throws Exception {
		proxy = mock(Private.class);
		final FluentApiExecutor executor = new WsFluentApiExecutor(proxy);

		api = new FluentApi(executor);
	}

	protected Private proxy() {
		return proxy;
	}

	protected FluentApi api() {
		return api;
	}

	/*
	 * Utils
	 */

	protected static String randomString() {
		return random(DEFAULT_RANDOM_STRING_COUNT);
	}

	protected Card cardCapturer() {
		return cardCaptor.capture();
	}

	protected Card capturedCard() {
		return cardCaptor.getValue();
	}

	protected Relation relationCapturer() {
		return relationCaptor.capture();
	}

	protected Relation capturedRelation() {
		return relationCaptor.getValue();
	}

	protected Query queryCapturer() {
		return queryCaptor.capture();
	}

	protected Query capturedQuery() {
		return queryCaptor.getValue();
	}

}
