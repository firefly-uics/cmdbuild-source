package unit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.cmdbuild.common.Constants;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.workflow.api.SharkWorkflowApi;
import org.cmdbuild.workflow.api.SharkWsWorkflowApi;
import org.cmdbuild.workflow.api.WorkflowApi;
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

	private WorkflowApi api;

	@Before
	public void setUp() throws Exception {
		proxy = mock(Private.class);

		final SharkWsWorkflowApi api = new SharkWsWorkflowApi();
		api.setProxy(proxy);
		this.api = api.workflowApi();
	}

	@Test
	@SuppressWarnings({ "unchecked" })
	public void selectReferenceCalledAsExpected() throws Exception {
		final CardList cardListMock = mock(CardList.class);
		final Card cardMock = mock(Card.class);
		when(cardListMock.getCards()).thenReturn(Arrays.asList(cardMock));
		when(cardMock.getId()).thenReturn(100);
		when(cardMock.getAttributeList()).thenReturn(
				Arrays.asList(attribute(Constants.CLASS_ID_ATTRIBUTE, "6"),
						attribute(Constants.DESCRIPTION_ATTRIBUTE, "dd")));
		when(proxy.getCardList(any(String.class), any(List.class), any(Query.class), //
				any(List.class), any(Integer.class), any(Integer.class), any(String.class), any(CqlQuery.class)))
				.thenReturn(cardListMock);

		final ReferenceType out = api.selectReference("foo", "bar", "baz");

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
		final Attribute a = new Attribute();
		a.setName(name);
		a.setValue(value);
		return a;
	}
}
