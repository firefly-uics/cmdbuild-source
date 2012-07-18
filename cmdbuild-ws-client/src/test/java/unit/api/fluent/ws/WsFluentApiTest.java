package unit.api.fluent.ws;

import static org.apache.commons.lang.RandomStringUtils.random;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;

import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class WsFluentApiTest {

	private static final int DEFAULT_RANDOM_STRING_COUNT = 10;

	private static final String CLASSNAME = "classname";

	private static final String ATTRIBUTE_1 = randomString();
	private static final String ATTRIBUTE_2 = randomString();
	private static final String MISSING_ATTRIBUTE = randomString();

	private static final String CODE_VALUE = randomString();
	private static final String DESCRIPTION_VALUE = randomString();
	private static final String ATTRIBUTE_1_VALUE = randomString();
	private static final String ATTRIBUTE_2_VALUE = randomString();

	private static final int CARD_ID = 123;
	private static final int NEW_CARD_ID = 42;

	private Private proxy;
	private FluentApiExecutor executor;
	private NewCard newCard;
	private ExistingCard existingCard;

	private ArgumentCaptor<Card> cardCaptor;

	@Before
	public void setUp() throws Exception {
		proxy = mock(Private.class);
		executor = new WsFluentApiExecutor(proxy);

		final FluentApi api = new FluentApi(executor);

		newCard = api //
				.newCard() //
				.forClass(CLASSNAME) //
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE);

		existingCard = api //
				.existingCard() //
				.forClass(CLASSNAME) //
				.withId(CARD_ID) // \
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE);

		cardCaptor = ArgumentCaptor.forClass(Card.class);
	}

	@Test
	public void parametersPassedToProxyWhenNewCardCreated() throws Exception {
		newCard.create();

		verify(proxy).createCard(cardCapturer());
		verifyNoMoreInteractions(proxy);

		final Card wsCard = capturedCard();
		assertThat(wsCard.getClassName(), equalTo(CLASSNAME));
		assertThat(wsCard.getAttributeList(), containsAttribute(CODE_ATTRIBUTE, CODE_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(DESCRIPTION_ATTRIBUTE, DESCRIPTION_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_1, ATTRIBUTE_1_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE));
		assertThat(wsCard.getAttributeList(), not(containsAttribute(MISSING_ATTRIBUTE)));
	}

	@Test
	public void idIsValueReturnedFromProxyWhenNewCardCreated() throws Exception {
		when(proxy.createCard(any(Card.class))).thenReturn(-NEW_CARD_ID);
		assertThat(newCard.create().getId(), is(-NEW_CARD_ID));

		when(proxy.createCard(any(Card.class))).thenReturn(NEW_CARD_ID);
		assertThat(newCard.create().getId(), is(NEW_CARD_ID));
	}

	@Test
	public void parametersPassedToProxyWhenUpdatingExistingCard() {
		existingCard.update();

		verify(proxy).updateCard(cardCapturer());
		verifyNoMoreInteractions(proxy);

		final Card wsCard = capturedCard();
		assertThat(wsCard.getClassName(), equalTo(CLASSNAME));
		assertThat(wsCard.getId(), equalTo(CARD_ID));
		assertThat(wsCard.getAttributeList(), containsAttribute(CODE_ATTRIBUTE, CODE_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(DESCRIPTION_ATTRIBUTE, DESCRIPTION_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_1, ATTRIBUTE_1_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE));
	}

	@Test
	public void parametersPassedToProxyWhenDeletingExistingCard() {
		existingCard.delete();

		verify(proxy).deleteCard(CLASSNAME, CARD_ID);
		verifyNoMoreInteractions(proxy);
	}

	/*
	 * Utils
	 */

	private static String randomString() {
		return random(DEFAULT_RANDOM_STRING_COUNT);
	}

	private Card cardCapturer() {
		return cardCaptor.capture();
	}

	private Card capturedCard() {
		return cardCaptor.getValue();
	}

}
