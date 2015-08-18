package unit.logic.filter;

import static com.google.common.reflect.Reflection.newProxy;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.common.utils.PagedElements.empty;
import static org.cmdbuild.common.utils.Reflection.unsupported;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.filter.DefaultFilterLogic;
import org.cmdbuild.logic.filter.DefaultFilterLogic.Converter;
import org.cmdbuild.logic.filter.FilterLogic.Filter;
import org.cmdbuild.services.store.filter.FilterDTO;
import org.cmdbuild.services.store.filter.FilterStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DefaultFilterLogicTest {

	private FilterStore store;
	private Converter converter;
	private UserStore userStore;
	private DefaultFilterLogic defaultFilterLogic;

	private AuthenticatedUser authenticatedUser;
	private PrivilegeContext privilegeContext;

	@Before
	public void setUp() throws Exception {
		store = mock(FilterStore.class);
		converter = mock(Converter.class);
		userStore = mock(UserStore.class);
		defaultFilterLogic = new DefaultFilterLogic(store, converter, userStore);

		authenticatedUser = mock(AuthenticatedUser.class);
		privilegeContext = mock(PrivilegeContext.class);
		final CMGroup selectedGroup = newProxy(CMGroup.class, unsupported("method not supported"));
		final OperationUser operationUser = new OperationUser(authenticatedUser, privilegeContext, selectedGroup);
		doReturn(operationUser) //
				.when(userStore).getUser();

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
	public void filterCreated() throws Exception {
		// given
		final Filter input = mock(Filter.class);
		doReturn("filter name") //
				.when(input).getName();
		final FilterStore.Filter convertedForStore = mock(FilterStore.Filter.class);
		final Filter convertedForOutput = mock(Filter.class);
		final FilterStore.Filter created = mock(FilterStore.Filter.class);
		doReturn(42L) //
				.when(store).create(any(FilterStore.Filter.class));
		doReturn(created) //
				.when(store).fetchFilter(anyLong());
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
		verify(store).fetchFilter(eq(42L));
		verify(converter).storeToLogic(eq(created));
		verifyNoMoreInteractions(store, converter, userStore, authenticatedUser, privilegeContext);
	}

	@Test
	public void onlySomeAttributesCanBeChangedDuringUpdate() throws Exception {
		// given
		final Filter input = mock(Filter.class);
		final FilterStore.Filter convertedForStore = FilterDTO.newFilter() //
				.withId(12L) //
				.withName("foo") //
				.withDescription("foo description") //
				.forClass("foo classname") //
				.withValue("foo value") //
				.asTemplate(true) //
				.withOwner(34L) //
				.build();
		doReturn(convertedForStore) //
				.when(converter).logicToStore(any(Filter.class));
		final FilterStore.Filter alreadyStored = FilterDTO.newFilter() //
				.withId(56L) //
				.withName("bar") //
				.withDescription("bar description") //
				.forClass("bar classname") //
				.withValue("bar value") //
				.asTemplate(false) //
				.withOwner(78L) //
				.build();
		doReturn(alreadyStored) //
				.when(store).fetchFilter(anyLong());

		// when
		defaultFilterLogic.update(input);

		// then
		final ArgumentCaptor<FilterStore.Filter> captor = ArgumentCaptor.forClass(FilterStore.Filter.class);
		verify(converter).logicToStore(eq(input));
		verify(store).fetchFilter(eq(12L));
		verify(store).update(captor.capture());
		verifyNoMoreInteractions(store, converter, userStore, authenticatedUser, privilegeContext);

		final FilterStore.Filter captured = captor.getValue();
		assertThat(captured.getId(), equalTo(alreadyStored.getId()));
		assertThat(captured.getName(), equalTo(convertedForStore.getName()));
		assertThat(captured.getDescription(), equalTo(convertedForStore.getDescription()));
		assertThat(captured.getClassName(), equalTo(convertedForStore.getClassName()));
		assertThat(captured.getValue(), equalTo(convertedForStore.getValue()));
		assertThat(captured.isTemplate(), equalTo(alreadyStored.isTemplate()));
		assertThat(captured.getOwner(), equalTo(alreadyStored.getOwner()));
	}

	@Test
	public void filterDeleted() throws Exception {
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
		verifyNoMoreInteractions(store, converter, userStore, authenticatedUser, privilegeContext);
	}

	@Test
	public void positionRequested() throws Exception {
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
		verifyNoMoreInteractions(store, converter, userStore, authenticatedUser, privilegeContext);
	}

	@Test
	public void filtersForCurrentUserAreRequested_OnlyUserFiltersAreReturned() throws Exception {
		// given
		final FilterStore.Filter first = mock(FilterStore.Filter.class);
		final FilterStore.Filter second = mock(FilterStore.Filter.class);
		doReturn(new PagedElements<FilterStore.Filter>(asList(first, second), 123)) //
				.when(store).getAllUserFilters(anyString(), anyLong(), anyInt(), anyInt());
		doReturn(empty()) //
				.when(store).fetchAllGroupsFilters(anyString(), anyInt(), anyInt());
		final Filter _first = mock(Filter.class);
		final Filter _second = mock(Filter.class);
		doReturn(_first).doReturn(_second) //
				.when(converter).storeToLogic(any(FilterStore.Filter.class));
		doReturn(42L) //
				.when(authenticatedUser).getId();

		// when
		final PagedElements<Filter> output = defaultFilterLogic.getFiltersForCurrentUser("a classname");

		// then
		assertThat(output.elements(), containsInAnyOrder(_first, _second));
		assertThat(output.totalSize(), equalTo(0));

		final ArgumentCaptor<FilterStore.Filter> captor = ArgumentCaptor.forClass(FilterStore.Filter.class);

		verify(userStore).getUser();
		verify(authenticatedUser).getId();
		verify(store).getAllUserFilters(eq("a classname"), eq(42L), eq(0), eq(MAX_VALUE));
		verify(store).fetchAllGroupsFilters(eq("a classname"), eq(0), eq(MAX_VALUE));
		verify(converter, times(2)).storeToLogic(captor.capture());
		verifyNoMoreInteractions(store, converter, userStore, authenticatedUser, privilegeContext);

		assertThat(captor.getAllValues().get(0), equalTo(first));
		assertThat(captor.getAllValues().get(1), equalTo(second));
	}

	@Test
	public void filtersForCurrentUserAreRequested_OnlyGroupFiltersAreReturned_UserHasAdministratorPrivileges()
			throws Exception {
		// given
		doReturn(empty()) //
				.when(store).getAllUserFilters(anyString(), anyLong(), anyInt(), anyInt());
		final FilterStore.Filter first = mock(FilterStore.Filter.class);
		final FilterStore.Filter second = mock(FilterStore.Filter.class);
		doReturn(new PagedElements<FilterStore.Filter>(asList(first, second), 123)) //
				.when(store).fetchAllGroupsFilters(anyString(), anyInt(), anyInt());
		final Filter _first = mock(Filter.class);
		final Filter _second = mock(Filter.class);
		doReturn(_first).doReturn(_second) //
				.when(converter).storeToLogic(any(FilterStore.Filter.class));
		doReturn(42L) //
				.when(authenticatedUser).getId();
		doReturn(true) //
				.when(privilegeContext).hasAdministratorPrivileges();

		// when
		final PagedElements<Filter> output = defaultFilterLogic.getFiltersForCurrentUser("a classname");

		// then
		assertThat(output.elements(), containsInAnyOrder(_first, _second));
		assertThat(output.totalSize(), equalTo(0));

		final ArgumentCaptor<FilterStore.Filter> captor = ArgumentCaptor.forClass(FilterStore.Filter.class);

		verify(userStore).getUser();
		verify(authenticatedUser).getId();
		verify(store).getAllUserFilters(eq("a classname"), eq(42L), eq(0), eq(MAX_VALUE));
		verify(store).fetchAllGroupsFilters(eq("a classname"), eq(0), eq(MAX_VALUE));
		verify(privilegeContext, times(2)).hasAdministratorPrivileges();
		verify(converter, times(2)).storeToLogic(captor.capture());
		verifyNoMoreInteractions(store, converter, userStore, authenticatedUser, privilegeContext);

		assertThat(captor.getAllValues().get(0), equalTo(first));
		assertThat(captor.getAllValues().get(1), equalTo(second));
	}

	@Test
	public void filtersForCurrentUserAreRequested_OnlyGroupFiltersAreReturned_UserHasNotAdministratorPrivilegesButReadAccess()
			throws Exception {
		// given
		doReturn(empty()) //
				.when(store).getAllUserFilters(anyString(), anyLong(), anyInt(), anyInt());
		final FilterStore.Filter first = mock(FilterStore.Filter.class);
		final FilterStore.Filter second = mock(FilterStore.Filter.class);
		doReturn(new PagedElements<FilterStore.Filter>(asList(first, second), 123)) //
				.when(store).fetchAllGroupsFilters(anyString(), anyInt(), anyInt());
		final Filter _first = mock(Filter.class);
		final Filter _second = mock(Filter.class);
		doReturn(_first).doReturn(_second) //
				.when(converter).storeToLogic(any(FilterStore.Filter.class));
		doReturn(42L) //
				.when(authenticatedUser).getId();
		doReturn(false) //
				.when(privilegeContext).hasAdministratorPrivileges();
		doReturn(true) //
				.when(privilegeContext).hasReadAccess(any(FilterStore.Filter.class));

		// when
		final PagedElements<Filter> output = defaultFilterLogic.getFiltersForCurrentUser("a classname");

		// then
		assertThat(output.elements(), containsInAnyOrder(_first, _second));
		assertThat(output.totalSize(), equalTo(0));

		final ArgumentCaptor<CMPrivilegedObject> privilegeContextCaptor = ArgumentCaptor
				.forClass(CMPrivilegedObject.class);
		final ArgumentCaptor<FilterStore.Filter> converterCaptor = ArgumentCaptor.forClass(FilterStore.Filter.class);

		verify(userStore).getUser();
		verify(authenticatedUser).getId();
		verify(store).getAllUserFilters(eq("a classname"), eq(42L), eq(0), eq(MAX_VALUE));
		verify(store).fetchAllGroupsFilters(eq("a classname"), eq(0), eq(MAX_VALUE));
		verify(privilegeContext, times(2)).hasAdministratorPrivileges();
		verify(privilegeContext, times(2)).hasReadAccess(privilegeContextCaptor.capture());
		verify(converter, times(2)).storeToLogic(converterCaptor.capture());
		verifyNoMoreInteractions(store, converter, userStore, authenticatedUser, privilegeContext);

		assertThat(privilegeContextCaptor.getAllValues().get(0), equalTo(CMPrivilegedObject.class.cast(first)));
		assertThat(privilegeContextCaptor.getAllValues().get(1), equalTo(CMPrivilegedObject.class.cast(second)));

		assertThat(converterCaptor.getAllValues().get(0), equalTo(first));
		assertThat(converterCaptor.getAllValues().get(1), equalTo(second));
	}

	@Test
	public void allFiltersAreRequested() throws Exception {
		// given
		final FilterStore.Filter first = mock(FilterStore.Filter.class);
		final FilterStore.Filter second = mock(FilterStore.Filter.class);
		doReturn(new PagedElements<FilterStore.Filter>(asList(first, second), 42)) //
				.when(store).fetchAllGroupsFilters(anyString(), anyInt(), anyInt());
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

		verify(store).fetchAllGroupsFilters(isNull(String.class), eq(123), eq(456));
		verify(converter, times(2)).storeToLogic(captor.capture());
		verifyNoMoreInteractions(store, converter, userStore, authenticatedUser, privilegeContext);

		assertThat(captor.getAllValues().get(0), equalTo(first));
		assertThat(captor.getAllValues().get(1), equalTo(second));
	}

	@Test
	public void allFiltersForSpecificClassAreRequested() throws Exception {
		// given
		final FilterStore.Filter first = mock(FilterStore.Filter.class);
		final FilterStore.Filter second = mock(FilterStore.Filter.class);
		doReturn(new PagedElements<FilterStore.Filter>(asList(first, second), 42)) //
				.when(store).getAllUserFilters(anyString(), anyLong(), anyInt(), anyInt());
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

		verify(store).getAllUserFilters(eq("a classname"), isNull(Long.class), eq(123), eq(456));
		verify(converter, times(2)).storeToLogic(captor.capture());
		verifyNoMoreInteractions(store, converter, userStore, authenticatedUser, privilegeContext);

		assertThat(captor.getAllValues().get(0), equalTo(first));
		assertThat(captor.getAllValues().get(1), equalTo(second));
	}

}
