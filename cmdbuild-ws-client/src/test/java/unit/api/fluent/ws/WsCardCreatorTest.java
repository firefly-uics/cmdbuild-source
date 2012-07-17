package unit.api.fluent.ws;

import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;

import org.cmdbuild.api.fluent.CardCreator;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.services.soap.Card;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class WsCardCreatorTest extends AbstractWsFluentApiTest {

	private static final String CLASSNAME = "classname";

	private static final String ATTRIBUTE_1 = randomString();
	private static final String ATTRIBUTE_2 = randomString();
	private static final String MISSING_ATTRIBUTE = randomString();

	private static final String CODE_VALUE = randomString();
	private static final String DESCRIPTION_VALUE = randomString();
	private static final String ATTRIBUTE_1_VALUE = randomString();
	private static final String ATTRIBUTE_2_VALUE = randomString();

	private static final int NEW_CARD_ID = 42;

	private CardCreator cardCreator;

	@Before
	public void createCardCreator() {
		cardCreator = api() //
				.newCard() //
				.forClass(CLASSNAME) //
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE);
	}

	@Test
	public void creationInvokesProxy() {
		when(proxy().createCard(any(Card.class))).thenReturn(NEW_CARD_ID);

		final CardDescriptor cardDescriptor = cardCreator.create();

		final ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
		verify(proxy()).createCard(captor.capture());
		verifyNoMoreInteractions(proxy());

		final Card wsCard = captor.getValue();
		assertThat(wsCard.getClassName(), equalTo(CLASSNAME));
		assertThat(wsCard.getAttributeList(), containsAttribute(CODE_ATTRIBUTE, CODE_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(DESCRIPTION_ATTRIBUTE, DESCRIPTION_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_1, ATTRIBUTE_1_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE));
		assertThat(wsCard.getAttributeList(), not(containsAttribute(MISSING_ATTRIBUTE)));

		assertThat(cardDescriptor.getClassName(), equalTo(CLASSNAME));
		assertThat(cardDescriptor.getId(), equalTo(NEW_CARD_ID));
	}

}
