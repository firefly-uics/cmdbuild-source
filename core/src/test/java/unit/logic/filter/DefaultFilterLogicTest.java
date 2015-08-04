package unit.logic.filter;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.filter.DefaultFilterLogic;
import org.cmdbuild.logic.filter.DefaultFilterLogic.Converter;
import org.cmdbuild.logic.filter.FilterLogic.Filter;
import org.cmdbuild.services.store.FilterStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DefaultFilterLogicTest {

	private FilterStore store;
	private Converter converter;
	private DefaultFilterLogic defaultFilterLogic;

	@Before
	public void setUp() throws Exception {
		store = mock(FilterStore.class);
		converter = mock(Converter.class);
		defaultFilterLogic = new DefaultFilterLogic(store, converter);
	}

	@Test(expected = NullPointerException.class)
	public void filterCannotBeCreatedWhenNameIsNull() throws Exception {
		// given
		final Filter input = mock(Filter.class);

		// when
		defaultFilterLogic.create(input);
	}

	@Test(expected = IllegalArgumentException.class)
	public void filterCannotBeCreatedWhenNameIsEmpty() throws Exception {
		// given
		final Filter input = mock(Filter.class);
		doReturn(EMPTY) //
				.when(input).getName();

		// when
		defaultFilterLogic.create(input);
	}

	@Test(expected = IllegalArgumentException.class)
	public void filterCannotBeCreatedWhenNameIsBlank() throws Exception {
		// given
		final Filter input = mock(Filter.class);
		doReturn(" ") //
				.when(input).getName();

		// when
		defaultFilterLogic.create(input);
	}

	@Test
	public void filterIsCreated() throws Exception {
		// given
		final Filter input = mock(Filter.class);
		doReturn("filter name") //
				.when(input).getName();
		final FilterStore.Filter convertedForStore = mock(FilterStore.Filter.class);
		final Filter convertedForOutput = mock(Filter.class);
		final FilterStore.Filter created = mock(FilterStore.Filter.class);
		doReturn(created) //
				.when(store).create(any(FilterStore.Filter.class));
		doReturn(convertedForStore) //
				.when(converter).logicToStore(any(Filter.class));
		doReturn(convertedForOutput) //
				.when(converter).storeToLogic(any(FilterStore.Filter.class));

		// when
		final Filter output = defaultFilterLogic.create(input);

		// then
		assertThat(output, equalTo(convertedForOutput));

		verify(converter).logicToStore(eq(input));
		verify(store).create(eq(convertedForStore));
		verify(converter).storeToLogic(eq(created));
		verifyNoMoreInteractions(store, converter);
	}

	@Test
	public void filterIsUpdated() throws Exception {
		// given
		final Filter input = mock(Filter.class);
		final FilterStore.Filter convertedForStore = mock(FilterStore.Filter.class);
		doReturn(convertedForStore) //
				.when(converter).logicToStore(any(Filter.class));

		// when
		defaultFilterLogic.update(input);

		// then
		verify(converter).logicToStore(eq(input));
		verify(store).update(eq(convertedForStore));
		verifyNoMoreInteractions(store, converter);
	}

	@Test
	public void filterIsDeleted() throws Exception {
		// given
		final Filter input = mock(Filter.class);
		final FilterStore.Filter convertedForStore = mock(FilterStore.Filter.class);
		doReturn(convertedForStore) //
				.when(converter).logicToStore(any(Filter.class));

		// when
		defaultFilterLogic.delete(input);

		// then
		verify(converter).logicToStore(eq(input));
		verify(store).delete(eq(convertedForStore));
		verifyNoMoreInteractions(store, converter);
	}

	@Test
	public void positionIsRequested() throws Exception {
		// given(Filter.class)
		final Filter input = mock(Filter.class);
		final FilterStore.Filter convertedForStore = mock(FilterStore.Filter.class);
		doReturn(convertedForStore) //
				.when(converter).logicToStore(any(Filter.class));

		// when
		defaultFilterLogic.position(input);

		// then
		verify(converter).logicToStore(eq(input));
		verify(store).getPosition(eq(convertedForStore));
		verifyNoMoreInteractions(store, converter);
	}

	@Test
	public void allFiltersForCurrentUserAreRequested() throws Exception {
		// given
		final FilterStore.Filter first = mock(FilterStore.Filter.class);
		final FilterStore.Filter second = mock(FilterStore.Filter.class);
		doReturn(new PagedElements<FilterStore.Filter>(asList(first, second), 42)) //
				.when(store).getFiltersForCurrentlyLoggedUser(anyString());
		final Filter _first = mock(Filter.class);
		final Filter _second = mock(Filter.class);
		doReturn(_first).doReturn(_second) //
				.when(converter).storeToLogic(any(FilterStore.Filter.class));

		// when
		final PagedElements<Filter> output = defaultFilterLogic.getFiltersForCurrentlyLoggedUser("a classname");

		// then
		assertThat(output.elements(), containsInAnyOrder(_first, _second));
		assertThat(output.totalSize(), equalTo(42));

		final ArgumentCaptor<FilterStore.Filter> captor = ArgumentCaptor.forClass(FilterStore.Filter.class);

		verify(store).getFiltersForCurrentlyLoggedUser(eq("a classname"));
		verify(converter, times(2)).storeToLogic(captor.capture());
		verifyNoMoreInteractions(store, converter);

		assertThat(captor.getAllValues().get(0), equalTo(first));
		assertThat(captor.getAllValues().get(1), equalTo(second));
	}

	@Test
	public void allFiltersAreRequested() throws Exception {
		// given
		final FilterStore.Filter first = mock(FilterStore.Filter.class);
		final FilterStore.Filter second = mock(FilterStore.Filter.class);
		doReturn(new PagedElements<FilterStore.Filter>(asList(first, second), 42)) //
				.when(store).fetchAllGroupsFilters(anyInt(), anyInt());
		final Filter _first = mock(Filter.class);
		final Filter _second = mock(Filter.class);
		doReturn(_first).doReturn(_second) //
				.when(converter).storeToLogic(any(FilterStore.Filter.class));

		// when
		final PagedElements<Filter> output = defaultFilterLogic.fetchAllGroupsFilters(123, 456);

		// then
		assertThat(output.elements(), containsInAnyOrder(_first, _second));
		assertThat(output.totalSize(), equalTo(42));

		final ArgumentCaptor<FilterStore.Filter> captor = ArgumentCaptor.forClass(FilterStore.Filter.class);

		verify(store).fetchAllGroupsFilters(eq(123), eq(456));
		verify(converter, times(2)).storeToLogic(captor.capture());
		verifyNoMoreInteractions(store, converter);

		assertThat(captor.getAllValues().get(0), equalTo(first));
		assertThat(captor.getAllValues().get(1), equalTo(second));
	}

	@Test
	public void allFiltersForSpecificClassAreRequested() throws Exception {
		// given
		final FilterStore.Filter first = mock(FilterStore.Filter.class);
		final FilterStore.Filter second = mock(FilterStore.Filter.class);
		doReturn(new PagedElements<FilterStore.Filter>(asList(first, second), 42)) //
				.when(store).getAllUserFilters(anyString(), anyInt(), anyInt());
		final Filter _first = mock(Filter.class);
		final Filter _second = mock(Filter.class);
		doReturn(_first).doReturn(_second) //
				.when(converter).storeToLogic(any(FilterStore.Filter.class));

		// when
		final PagedElements<Filter> output = defaultFilterLogic.getAllUserFilters("a classname", 123, 456);

		// then
		assertThat(output.elements(), containsInAnyOrder(_first, _second));
		assertThat(output.totalSize(), equalTo(42));

		final ArgumentCaptor<FilterStore.Filter> captor = ArgumentCaptor.forClass(FilterStore.Filter.class);

		verify(store).getAllUserFilters(eq("a classname"), eq(123), eq(456));
		verify(converter, times(2)).storeToLogic(captor.capture());
		verifyNoMoreInteractions(store, converter);

		assertThat(captor.getAllValues().get(0), equalTo(first));
		assertThat(captor.getAllValues().get(1), equalTo(second));
	}

}
