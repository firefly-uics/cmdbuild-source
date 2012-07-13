package unit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;

import java.util.Map;
import java.util.TreeMap;

import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Relation;
import org.cmdbuild.workflow.api.SharkWorkflowApi;
import org.cmdbuild.workflow.api.SharkWsWorkflowApi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests that the web service implementation of {@link SharkWorkflowApi}
 * calls the SOAP proxy correctly.
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
		attributes.put("Code", "bar");
		attributes.put("Description", "baz");

		final int id = api.createCard("foo", attributes);

		final ArgumentCaptor<Card> argument = ArgumentCaptor.forClass(Card.class);
		verify(proxy).createCard(argument.capture());
		verifyNoMoreInteractions(proxy);

		assertThat(argument.getValue().getClassName(), equalTo("foo"));
		assertThat(argument.getValue().getAttributeList(), containsAttribute("Code", "bar"));
		assertThat(argument.getValue().getAttributeList(), containsAttribute("Description", "baz"));
		assertThat(argument.getValue().getAttributeList(), not(containsAttribute("Dummy", "dummy")));
		assertThat(id, equalTo(42));
	}

	@Test
	public void createRelationCalledAsExpected() throws Exception {
		api.createRelation("foo", "bar", 1, "baz", 2);

		final ArgumentCaptor<Relation> argument = ArgumentCaptor.forClass(Relation.class);
		verify(proxy).createRelation(argument.capture());
		verifyNoMoreInteractions(proxy);

		assertThat(argument.getValue().getDomainName(), equalTo("foo"));
		assertThat(argument.getValue().getClass1Name(), equalTo("bar"));
		assertThat(argument.getValue().getCard1Id(), equalTo(1));
		assertThat(argument.getValue().getClass2Name(), equalTo("baz"));
		assertThat(argument.getValue().getCard2Id(), equalTo(2));
	}

}
