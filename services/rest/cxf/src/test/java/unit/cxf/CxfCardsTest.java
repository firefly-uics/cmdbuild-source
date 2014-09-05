package unit.cxf;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.HashMap;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.ClassCards;
import org.cmdbuild.service.rest.cxf.CxfCards;
import org.cmdbuild.service.rest.dto.Card;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CxfCardsTest {

	@Captor
	private ArgumentCaptor<MultivaluedMap<String, String>> multivaluedMapCaptor;

	private ClassCards classCards;

	private CxfCards cxfCards;

	@Before
	public void setUp() throws Exception {
		classCards = mock(ClassCards.class);
		cxfCards = new CxfCards(classCards);
	}

	@Test
	public void createDelegated() throws Exception {
		// given
		final MultivaluedMap<String, String> params = new MetadataMap<String, String>();
		params.put("foo", asList("bar"));
		params.put("bar", asList("baz"));
		params.put("baz", asList("foo"));

		// when
		cxfCards.create(params, "foo");

		// then
		verify(classCards).create(eq("foo"), multivaluedMapCaptor.capture());
		verifyNoMoreInteractions(classCards);

		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst("foo"), equalTo((Object) "bar"));
		assertThat(captured.getFirst("bar"), equalTo((Object) "baz"));
		assertThat(captured.getFirst("baz"), equalTo((Object) "foo"));
	}

	@Test
	public void readDelegated() throws Exception {
		// given
		final SimpleResponse<Card> expectedResponse = SimpleResponse.newInstance(Card.class) //
				.withElement(Card.newInstance() //
						.withType("foo") //
						.withId(123L) //
						.withValues(ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut("foo", "bar") //
								.chainablePut("bar", "baz") //
								.chainablePut("baz", "foo")) //
						.build() //
				) //
				.build();
		doReturn(expectedResponse) //
				.when(classCards).read(anyString(), anyLong());

		// when
		final SimpleResponse<Card> response = cxfCards.read("foo", 123L);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(classCards).read("foo", 123L);
		verifyNoMoreInteractions(classCards);
	}

	@Test
	public void readAllDelegated() throws Exception {
		// given
		final ListResponse<Card> expectedResponse = ListResponse.newInstance(Card.class) //
				.withElements(Arrays.asList(Card.newInstance() //
						.withType("foo") //
						.withId(123L) //
						.withValues(ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut("foo", "bar") //
								.chainablePut("bar", "baz") //
								.chainablePut("baz", "foo")) //
						.build() //
						)) //
				.build();
		doReturn(expectedResponse) //
				.when(classCards).read(anyString(), anyString(), anyInt(), anyInt());

		// when
		final ListResponse<Card> response = cxfCards.read("foo", "filter", 123, 456);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(classCards).read("foo", "filter", 123, 456);
		verifyNoMoreInteractions(classCards);
	}

	@Test
	public void updatedDelegated() throws Exception {
		// given
		final MultivaluedMap<String, String> params = new MetadataMap<String, String>();
		params.put("foo", asList("bar"));
		params.put("bar", asList("baz"));
		params.put("baz", asList("foo"));

		// when
		cxfCards.update(123L, params, "type");

		// then
		verify(classCards).update(eq("type"), eq(123L), multivaluedMapCaptor.capture());
		verifyNoMoreInteractions(classCards);

		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst("foo"), equalTo((Object) "bar"));
		assertThat(captured.getFirst("bar"), equalTo((Object) "baz"));
		assertThat(captured.getFirst("baz"), equalTo((Object) "foo"));
	}

	@Test
	public void deleteDelegated() throws Exception {
		// when
		cxfCards.delete("foo", 123L);

		// then
		verify(classCards).delete("foo", 123L);
		verifyNoMoreInteractions(classCards);
	}

}
