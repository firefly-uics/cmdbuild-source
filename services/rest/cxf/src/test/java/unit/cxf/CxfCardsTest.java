package unit.cxf;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_CLASSNAME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.ClassCards;
import org.cmdbuild.service.rest.cxf.CxfCards;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.service.rest.serialization.ErrorHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CxfCardsTest {

	@Captor
	private ArgumentCaptor<MultivaluedMap<String, String>> multivaluedMapCaptor;

	private ErrorHandler errorHandler;
	private ClassCards classCards;

	private CxfCards cxfCards;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		classCards = mock(ClassCards.class);
		cxfCards = new CxfCards(errorHandler, classCards);
	}

	@Test
	public void createRequiresClassNameWithinParameters() throws Exception {
		// given
		final MultivaluedMap<String, String> params = new MetadataMap<String, String>();
		params.put("foo", asList("bar"));
		params.put("bar", asList("baz"));
		params.put("baz", asList("foo"));

		// when
		cxfCards.create(params);

		// then
		final InOrder inOrder = inOrder(errorHandler, classCards);
		inOrder.verify(errorHandler).missingParam(UNDERSCORED_CLASSNAME);
		inOrder.verify(classCards).create(null, params);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void createDelegated() throws Exception {
		// given
		final MultivaluedMap<String, String> params = new MetadataMap<String, String>();
		params.put(UNDERSCORED_CLASSNAME, asList("foo"));
		params.put("foo", asList("bar"));
		params.put("bar", asList("baz"));
		params.put("baz", asList("foo"));

		// when
		cxfCards.create(params);

		// then
		verify(classCards).create(eq("foo"), multivaluedMapCaptor.capture());
		verifyNoMoreInteractions(errorHandler, classCards);

		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst(UNDERSCORED_CLASSNAME), is(nullValue()));
		assertThat(captured.getFirst("foo"), equalTo((Object) "bar"));
		assertThat(captured.getFirst("bar"), equalTo((Object) "baz"));
		assertThat(captured.getFirst("baz"), equalTo((Object) "foo"));
	}

	@Test
	public void readDelegated() throws Exception {
		// given
		final Map<String, Object> values = ChainablePutMap.of(new HashMap<String, Object>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz") //
				.chainablePut("baz", "foo");
		final SimpleResponse<Map<String, Object>> expectedResponse = SimpleResponse.<Map<String, Object>> newInstance() //
				.withElement(values) //
				.build();
		when(classCards.read(anyString(), anyLong())) //
				.thenReturn(expectedResponse);

		// when
		final SimpleResponse<Map<String, Object>> response = cxfCards.read("foo", 123L);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(classCards).read("foo", 123L);
		verifyNoMoreInteractions(errorHandler, classCards);
	}

	@Test
	public void readAllDelegated() throws Exception {
		// given
		final Map<String, Object> values = ChainablePutMap.of(new HashMap<String, Object>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz") //
				.chainablePut("baz", "foo");
		final ListResponse<Map<String, Object>> expectedResponse = ListResponse.<Map<String, Object>> newInstance() //
				.withElement(values) //
				.build();
		when(classCards.readAll(anyString(), anyString(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final ListResponse<Map<String, Object>> response = cxfCards.readAll("foo", "filter", 123, 456);

		// then
		assertThat(response, equalTo(expectedResponse));
		verify(classCards).readAll("foo", "filter", 123, 456);
		verifyNoMoreInteractions(errorHandler, classCards);
	}

	@Test
	public void updateRequiresClassNameWithinParameters() throws Exception {
		// given
		final MultivaluedMap<String, String> params = new MetadataMap<String, String>();
		params.put("foo", asList("bar"));
		params.put("bar", asList("baz"));
		params.put("baz", asList("foo"));

		// when
		cxfCards.update(123L, params);

		// then
		final InOrder inOrder = inOrder(errorHandler, classCards);
		inOrder.verify(errorHandler).missingParam(UNDERSCORED_CLASSNAME);
		inOrder.verify(classCards).update(null, 123L, params);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void updatedDelegated() throws Exception {
		// given
		final MultivaluedMap<String, String> params = new MetadataMap<String, String>();
		params.put(UNDERSCORED_CLASSNAME, asList("foo"));
		params.put("foo", asList("bar"));
		params.put("bar", asList("baz"));
		params.put("baz", asList("foo"));

		// when
		cxfCards.update(123L, params);

		// then
		verify(classCards).update(eq("foo"), eq(123L), multivaluedMapCaptor.capture());
		verifyNoMoreInteractions(errorHandler, classCards);

		final MultivaluedMap<String, String> captured = multivaluedMapCaptor.getValue();
		assertThat(captured.getFirst(UNDERSCORED_CLASSNAME), is(nullValue()));
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
		verifyNoMoreInteractions(errorHandler, classCards);
	}

}
