package unit.api.fluent.ws;

import static org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.soapCardFor;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;

import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.junit.Before;
import org.junit.Test;

public class ExistingCardTest extends AbstractWsFluentApiTest {

	private ExistingCard existingCard;

	@Before
	public void createExistingCard() throws Exception {
		existingCard = api() //
				.existingCard(CLASS_NAME, CARD_ID) // \
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE);
	}

	@Test
	public void parametersPassedToProxyWhenUpdatingExistingCard() {
		existingCard.update();

		verify(proxy()).updateCard(cardCapturer());
		verifyNoMoreInteractions(proxy());

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

		verify(proxy()).deleteCard(existingCard.getClassName(), existingCard.getId());
		verifyNoMoreInteractions(proxy());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void parametersPassedToProxyWhenFetchingExistingCard() {
		when(proxy().getCard( //
				eq(existingCard.getClassName()), //
				eq(existingCard.getId()), //
				anyListOf(Attribute.class))) //
				.thenReturn(soapCardFor(existingCard));

		existingCard.fetch();

		verify(proxy()).getCard( //
				eq(existingCard.getClassName()), //
				eq(existingCard.getId()), //
				argThat(allOf( //
						containsAttribute(CODE_ATTRIBUTE), //
						containsAttribute(DESCRIPTION_ATTRIBUTE), //
						containsAttribute(ATTRIBUTE_1), //
						containsAttribute(ATTRIBUTE_2))));
		verifyNoMoreInteractions(proxy());
	}

	@Test
	public void soapCardIsConvertedToFluentApiCardWhenFetchingExistingCard() {
		when(proxy().getCard( //
				eq(existingCard.getClassName()), //
				eq(existingCard.getId()), //
				anyListOf(Attribute.class))) //
				.thenReturn(soapCardFor(existingCard));

		final org.cmdbuild.api.fluent.Card card = existingCard.fetch();

		assertThat(card.getClassName(), equalTo(existingCard.getClassName()));
		assertThat(card.getAttributes(), hasEntry(CODE_ATTRIBUTE, (Object) CODE_VALUE));
		assertThat(card.getAttributes(), hasEntry(DESCRIPTION_ATTRIBUTE, (Object) DESCRIPTION_VALUE));
		assertThat(card.getAttributes(), hasEntry(ATTRIBUTE_1, (Object) ATTRIBUTE_1_VALUE));
		assertThat(card.getAttributes(), hasEntry(ATTRIBUTE_2, (Object) ATTRIBUTE_2_VALUE));
	}

}
