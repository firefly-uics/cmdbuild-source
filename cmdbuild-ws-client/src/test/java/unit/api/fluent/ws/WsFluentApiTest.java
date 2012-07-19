package unit.api.fluent.ws;

import static org.apache.commons.lang.RandomStringUtils.random;
import static org.cmdbuild.api.utils.SoapUtils.soapCardFor;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;

import java.util.Map;

import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.ExistingRelation;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.api.fluent.NewRelation;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Relation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class WsFluentApiTest {

	private static final int DEFAULT_RANDOM_STRING_COUNT = 10;

	private static final String CLASSNAME = "classname";
	private static final String ANOTHER_CLASSNAME = randomString();

	private static final String ATTRIBUTE_1 = randomString();
	private static final String ATTRIBUTE_2 = randomString();
	private static final String MISSING_ATTRIBUTE = randomString();

	private static final String CODE_VALUE = randomString();
	private static final String DESCRIPTION_VALUE = randomString();
	private static final String ATTRIBUTE_1_VALUE = randomString();
	private static final String ATTRIBUTE_2_VALUE = randomString();

	private static final String DOMAIN_NAME = "domainname";

	private static final int CARD_ID = 123;
	private static final int ANOTHER_CARD_ID = 321;
	private static final int NEW_CARD_ID = 42;

	private Private proxy;
	private FluentApiExecutor executor;

	private NewCard newCard;

	private ExistingCard existingCard;
	private Map<String, String> existingAttributes;

	private NewRelation newRelation;

	private ExistingRelation existingRelation;

	private final ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
	private final ArgumentCaptor<Relation> relationCaptor = ArgumentCaptor.forClass(Relation.class);

	@Before
	public void setUp() throws Exception {
		proxy = mock(Private.class);
		executor = new WsFluentApiExecutor(proxy);

		final FluentApi api = new FluentApi(executor);

		newCard = api //
				.newCard() //
				.forClassName(CLASSNAME) //
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE);

		existingCard = api //
				.existingCard() //
				.forClassName(CLASSNAME) //
				.withId(CARD_ID) // \
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE);
		existingAttributes = existingCard.getAttributes();

		newRelation = api.newRelation() //
				.withDomainName(DOMAIN_NAME) //
				.withCard1(CLASSNAME, CARD_ID) //
				.withCard2(ANOTHER_CLASSNAME, ANOTHER_CARD_ID);

		existingRelation = api.existingRelation() //
				.withDomainName(DOMAIN_NAME) //
				.withCard1(CLASSNAME, CARD_ID) //
				.withCard2(ANOTHER_CLASSNAME, ANOTHER_CARD_ID);
	}

	@Test
	public void parametersPassedToProxyWhenNewCardCreated() throws Exception {
		newCard.create();

		verify(proxy).createCard(cardCapturer());
		verifyNoMoreInteractions(proxy);

		final Card wsCard = capturedCard();
		assertThat(wsCard.getClassName(), equalTo(newCard.getClassName()));
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
		assertThat(wsCard.getClassName(), equalTo(existingCard.getClassName()));
		assertThat(wsCard.getId(), equalTo(existingCard.getId()));
		assertThat(wsCard.getAttributeList(), containsAttribute(CODE_ATTRIBUTE, CODE_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(DESCRIPTION_ATTRIBUTE, DESCRIPTION_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_1, ATTRIBUTE_1_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE));
	}

	@Test
	public void parametersPassedToProxyWhenDeletingExistingCard() {
		existingCard.delete();

		verify(proxy).deleteCard(existingCard.getClassName(), existingCard.getId());
		verifyNoMoreInteractions(proxy);
	}

	@Test
	public void parametersPassedToProxyWhenFetchingExistingCard() {
		when(proxy.getCard( //
				eq(existingCard.getClassName()), //
				eq(existingCard.getId()), //
				anyListOf(Attribute.class))) //
				.thenReturn(soapCardFor(existingCard));

		existingCard.fetch();

		verify(proxy).getCard( //
				eq(existingCard.getClassName()), //
				eq(existingCard.getId()), //
				argThat(allOf( //
						containsAttribute(CODE_ATTRIBUTE), //
						containsAttribute(DESCRIPTION_ATTRIBUTE), //
						containsAttribute(ATTRIBUTE_1), //
						containsAttribute(ATTRIBUTE_2))));
		verifyNoMoreInteractions(proxy);
	}

	@Test
	public void soapCardIsConvertedtoFluentApiCardWhenFetchingExistingCard() {
		when(proxy.getCard( //
				eq(existingCard.getClassName()), //
				eq(existingCard.getId()), //
				anyListOf(Attribute.class))) //
				.thenReturn(soapCardFor(existingCard));

		final org.cmdbuild.api.fluent.Card card = existingCard.fetch();

		assertThat(card.getClassName(), equalTo(existingCard.getClassName()));
		assertThat(card.getAttributes(), hasEntry(CODE_ATTRIBUTE, CODE_VALUE));
		assertThat(card.getAttributes(), hasEntry(DESCRIPTION_ATTRIBUTE, DESCRIPTION_VALUE));
		assertThat(card.getAttributes(), hasEntry(ATTRIBUTE_1, ATTRIBUTE_1_VALUE));
		assertThat(card.getAttributes(), hasEntry(ATTRIBUTE_2, ATTRIBUTE_2_VALUE));
	}

	@Test
	public void parametersPassedToProxyWhenCreatingNewRelation() {
		newRelation.create();

		verify(proxy).createRelation(relationCapturer());
		verifyNoMoreInteractions(proxy);

		final Relation wsRelation = capturedRelation();
		assertThat(wsRelation.getDomainName(), equalTo(newRelation.getDomainName()));
		assertThat(wsRelation.getClass1Name(), equalTo(newRelation.getClassName1()));
		assertThat(wsRelation.getCard1Id(), equalTo(newRelation.getClassId1()));
		assertThat(wsRelation.getClass2Name(), equalTo(newRelation.getClassName2()));
		assertThat(wsRelation.getCard2Id(), equalTo(newRelation.getClassId2()));
	}

	@Test
	public void parametersPassedToProxyWhenDeletingExistingRelation() {
		existingRelation.delete();

		verify(proxy).deleteRelation(relationCapturer());
		verifyNoMoreInteractions(proxy);

		final Relation wsRelation = capturedRelation();
		assertThat(wsRelation.getDomainName(), equalTo(newRelation.getDomainName()));
		assertThat(wsRelation.getClass1Name(), equalTo(newRelation.getClassName1()));
		assertThat(wsRelation.getCard1Id(), equalTo(newRelation.getClassId1()));
		assertThat(wsRelation.getClass2Name(), equalTo(newRelation.getClassName2()));
		assertThat(wsRelation.getCard2Id(), equalTo(newRelation.getClassId2()));
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

	private Relation relationCapturer() {
		return relationCaptor.capture();
	}

	private Relation capturedRelation() {
		return relationCaptor.getValue();
	}

}
