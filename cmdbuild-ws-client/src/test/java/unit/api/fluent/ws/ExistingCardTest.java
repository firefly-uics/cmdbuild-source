package unit.api.fluent.ws;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE) //
				.limitAttributes(ATTRIBUTE_3, ATTRIBUTE_4);
	}

	@Test
	public void parametersPassedToProxyWhenUpdatingExistingCard() throws Exception {
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
	public void parametersPassedToProxyWhenDeletingExistingCard() throws Exception {
		existingCard.delete();

		verify(proxy()).deleteCard(existingCard.getClassName(), existingCard.getId());
		verifyNoMoreInteractions(proxy());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void parametersPassedToProxyWhenFetchingExistingCard() throws Exception {
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
						containsAttribute(ATTRIBUTE_3), //
						containsAttribute(ATTRIBUTE_4))));
		verifyNoMoreInteractions(proxy());
	}

	@Test
	public void soapCardIsConvertedToFluentApiCardWhenFetchingExistingCard() throws Exception {
		when(proxy().getCard( //
				eq(existingCard.getClassName()), //
				eq(existingCard.getId()), //
				anyListOf(Attribute.class))) //
				.thenReturn(soapCardFor(existingCard));

		final org.cmdbuild.api.fluent.Card card = existingCard.fetch();

		assertThat(card.getClassName(), equalTo(existingCard.getClassName()));
		assertThat(card.getId(), equalTo(existingCard.getId()));
		assertThat(card.getAttributes(), hasEntry(CODE_ATTRIBUTE, (Object) CODE_VALUE));
		assertThat(card.getAttributes(), hasEntry(DESCRIPTION_ATTRIBUTE, (Object) DESCRIPTION_VALUE));
		assertThat(card.getAttributes(), hasEntry(ATTRIBUTE_1, (Object) ATTRIBUTE_1_VALUE));
		assertThat(card.getAttributes(), hasEntry(ATTRIBUTE_2, (Object) ATTRIBUTE_2_VALUE));
	}

	@Test
	public void referenceOrLookupAttributeValueIsReturnedAsStringRepresentationOfInteger() throws Exception {
		// FIXME test with mock type converter

		final ExistingCard existingCard = api().existingCard(CLASS_NAME, CARD_ID);

		when(proxy().getCard( //
				eq(existingCard.getClassName()), //
				eq(existingCard.getId()), //
				anyListOf(Attribute.class))) //
				.thenReturn(soapCardWithReference(CLASS_NAME, CARD_ID, ATTRIBUTE_1, ANOTHER_CARD_ID));

		final org.cmdbuild.api.fluent.Card card = existingCard.fetch();

		assertThat(card.getClassName(), equalTo(existingCard.getClassName()));
		assertThat(card.getId(), equalTo(existingCard.getId()));
		assertThat(card.getAttributes(), hasEntry(ATTRIBUTE_1, (Object) Integer.toString(ANOTHER_CARD_ID)));
	}

	private Card soapCardWithReference(final String className, final int cardId, final String referenceAttributeName,
			final int referenceId) {
		final Card card = new Card();
		card.setClassName(className);
		card.setId(CARD_ID);
		card.getAttributeList().add(new Attribute() {
			{
				setName(referenceAttributeName);
				setCode(Integer.toString(referenceId));
			}
		});
		return card;
	}

	/*
	 * Utils
	 */

	private Card soapCardFor(final org.cmdbuild.api.fluent.Card card) {
		final org.cmdbuild.services.soap.Card soapCard = new org.cmdbuild.services.soap.Card();
		soapCard.setClassName(card.getClassName());
		if (card.getId() != null) {
			soapCard.setId(card.getId());
		}
		soapCard.getAttributeList().addAll(attributesFor(card));
		return soapCard;
	}

	private List<Attribute> attributesFor(final org.cmdbuild.api.fluent.Card card) {
		final List<Attribute> attributes = new ArrayList<Attribute>();
		for (final Entry<String, Object> entry : card.getAttributes().entrySet()) {
			attributes.add(new Attribute() {
				{
					setName(entry.getKey());
					setValue(entry.getValue().toString());
				}
			});
		}
		return attributes;
	}

}
