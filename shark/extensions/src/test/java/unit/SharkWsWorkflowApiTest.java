package unit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.cmdbuild.common.Constants;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.services.soap.Relation;
import org.cmdbuild.workflow.api.SharkWorkflowApi;
import org.cmdbuild.workflow.api.SharkWsWorkflowApi;
import org.cmdbuild.workflow.type.ReferenceType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests that the web service implementation of {@link SharkWorkflowApi} calls
 * the SOAP proxy correctly.
 */
public class SharkWsWorkflowApiTest {

	private Private proxy;

	private SharkWorkflowApi api;

	@Before
	public void setUp() throws Exception {
		proxy = mock(Private.class);

		final SharkWsWorkflowApi api = new SharkWsWorkflowApi();
		api.setProxy(proxy);
		this.api = api;
	}

	@Test
	public void createCardCalledAsExpected() throws Exception {
		when(proxy.createCard(any(Card.class))).thenReturn(42);

		final Map<String, Object> attributes = new TreeMap<String, Object>();
		attributes.put(Constants.CODE_ATTRIBUTE, "bar");
		attributes.put(Constants.DESCRIPTION_ATTRIBUTE, "baz");

		final int id = api.createCard("foo", attributes);

		final ArgumentCaptor<Card> argument = ArgumentCaptor.forClass(Card.class);
		verify(proxy).createCard(argument.capture());
		verifyNoMoreInteractions(proxy);

		assertThat(argument.getValue().getClassName(), is("foo"));
		assertThat(argument.getValue().getAttributeList(), containsAttribute(Constants.CODE_ATTRIBUTE, "bar"));
		assertThat(argument.getValue().getAttributeList(), containsAttribute(Constants.DESCRIPTION_ATTRIBUTE, "baz"));
		assertThat(argument.getValue().getAttributeList(), not(containsAttribute("Dummy", "dummy")));
		assertThat(id, is(42));
	}

	@Test
	public void createRelationCalledAsExpected() throws Exception {
		api.createRelation("foo", "bar", 1, "baz", 2);

		final ArgumentCaptor<Relation> argument = ArgumentCaptor.forClass(Relation.class);
		verify(proxy).createRelation(argument.capture());
		verifyNoMoreInteractions(proxy);

		assertThat(argument.getValue().getDomainName(), is("foo"));
		assertThat(argument.getValue().getClass1Name(), is("bar"));
		assertThat(argument.getValue().getCard1Id(), is(1));
		assertThat(argument.getValue().getClass2Name(), is("baz"));
		assertThat(argument.getValue().getCard2Id(), is(2));
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void selectAttributeCalledAsExpected() throws Exception {
		Card cardMock = mock(Card.class);
		when(cardMock.getAttributeList()).thenReturn(Arrays.asList(attribute("bar", "barValue")));
		when(proxy.getCard(any(String.class), any(Integer.class), any(List.class))).thenReturn(cardMock);

		String out = api.selectAttribute("foo", 12, "bar");

		final ArgumentCaptor<String> classNameCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Integer> cardIdCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<List> attributeNameCaptor = ArgumentCaptor.forClass(List.class);
		verify(proxy).getCard(classNameCaptor.capture(), cardIdCaptor.capture(), attributeNameCaptor.capture());
		verifyNoMoreInteractions(proxy);

		assertThat(classNameCaptor.getValue(), is("foo"));
		assertThat(cardIdCaptor.getValue(), is(12));
		assertThat(attributeNameCaptor.getValue(), is(nullValue()));
		assertThat(out, is("barValue"));
	}

	@Test
	@SuppressWarnings({ "unchecked" })
	public void selectReferenceCalledAsExpected() throws Exception {
		CardList cardListMock = mock(CardList.class);
		Card cardMock = mock(Card.class);
		when(cardListMock.getCards()).thenReturn(Arrays.asList(cardMock));
		when(cardMock.getId()).thenReturn(100);
		when(cardMock.getAttributeList()).thenReturn(
				Arrays.asList(attribute(Constants.CLASS_ID_ATTRIBUTE, "6"),
						attribute(Constants.DESCRIPTION_ATTRIBUTE, "dd")));
		when(proxy.getCardList(any(String.class), any(List.class), any(Query.class), //
				any(List.class), any(Integer.class), any(Integer.class), any(String.class), any(CqlQuery.class)))
				.thenReturn(cardListMock);

		ReferenceType out = api.selectReference("foo", "bar", "baz");

		final ArgumentCaptor<String> classNameCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
		verify(proxy).getCardList(classNameCaptor.capture(), any(List.class), queryCaptor.capture(), //
				any(List.class), any(Integer.class), any(Integer.class), any(String.class), any(CqlQuery.class));
		verifyNoMoreInteractions(proxy);

		assertThat(classNameCaptor.getValue(), is("foo"));
		final Filter filter = queryCaptor.getValue().getFilter();
		assertThat(filter.getName(), is("bar"));
		assertThat(filter.getValue(), is(Arrays.asList("baz")));

		assertThat(out.getId(), is(100));
		assertThat(out.getIdClass(), is(6));
		assertThat(out.getDescription(), is("dd"));
	}

	/*
	 * Utils
	 */

	private Attribute attribute(final String name, final String value) {
		Attribute a = new Attribute();
		a.setName(name);
		a.setValue(value);
		return a;
	}
}
