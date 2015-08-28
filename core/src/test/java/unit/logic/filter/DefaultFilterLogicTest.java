package unit.logic.filter;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
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
				.withClassName("foo classname") //
				.withConfiguration("foo value") //
				.thatIsShared(true) //
				.withUserId(34L) //
				.build();
		doReturn(convertedForStore) //
				.when(converter).logicToStore(any(Filter.class));
		final FilterStore.Filter alreadyStored = FilterDTO.newFilter() //
				.withId(56L) //
				.withName("bar") //
				.withDescription("bar description") //
				.withClassName("bar classname") //
				.withConfiguration("bar value") //
				.thatIsShared(false) //
				.withUserId(78L) //
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
		assertThat(captured.getConfiguration(), equalTo(convertedForStore.getConfiguration()));
		assertThat(captured.isShared(), equalTo(alreadyStored.isShared()));
		assertThat(captured.getUserId(), equalTo(alreadyStored.getUserId()));
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
		final PagedElements<Filter> output = defaultFilterLogic.fetchAllGroupsFilters("foo", 123, 456);

		// then
		assertThat(output.elements(), containsInAnyOrder(_first, _second));
		assertThat(output.totalSize(), equalTo(42));

		final ArgumentCaptor<FilterStore.Filter> captor = ArgumentCaptor.forClass(FilterStore.Filter.class);

		verify(store).fetchAllGroupsFilters(eq("foo"), eq(123), eq(456));
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

	@Test
	public void allDefaultFiltersAreRequestedWithSpecificGroup() throws Exception {
		// given
		final FilterStore.Filter first = mock(FilterStore.Filter.class);
		final FilterStore.Filter second = mock(FilterStore.Filter.class);
		doReturn(asList(first, second)) //
				.when(store).getAllFilters(anyString(), anyString());
		final Filter _first = mock(Filter.class);
		final Filter _second = mock(Filter.class);
		doReturn(_first).doReturn(_second) //
				.when(converter).storeToLogic(any(FilterStore.Filter.class));

		// when
		final Iterable<Filter> output = newArrayList(defaultFilterLogic.getDefaults("a classname", "a group"));

		// then
		assertThat(size(output), equalTo(2));
		assertThat(output, containsInAnyOrder(_first, _second));

		final ArgumentCaptor<FilterStore.Filter> captor = ArgumentCaptor.forClass(FilterStore.Filter.class);

		verify(store).getAllFilters(eq("a classname"), eq("a group"));
		verify(converter, times(2)).storeToLogic(captor.capture());
		verifyNoMoreInteractions(store, converter, userStore, authenticatedUser, privilegeContext);

		assertThat(captor.getAllValues().get(0), equalTo(first));
		assertThat(captor.getAllValues().get(1), equalTo(second));
	}

	@Test
	public void allDefaultFiltersAreRequestedWithNoGroup() throws Exception {
		// given
		final CMGroup selectedGroup = mock(CMGroup.class);
		doReturn("a group") //
				.when(selectedGroup).getName();
		final OperationUser operationUser = new OperationUser(authenticatedUser, privilegeContext, selectedGroup);
		doReturn(operationUser) //
				.when(userStore).getUser();
		final FilterStore.Filter first = mock(FilterStore.Filter.class);
		final FilterStore.Filter second = mock(FilterStore.Filter.class);
		doReturn(asList(first, second)) //
				.when(store).getAllFilters(anyString(), anyString());
		final Filter _first = mock(Filter.class);
		final Filter _second = mock(Filter.class);
		doReturn(_first).doReturn(_second) //
				.when(converter).storeToLogic(any(FilterStore.Filter.class));

		// when
		final Iterable<Filter> output = newArrayList(defaultFilterLogic.getDefaults("a classname", null));

		// then
		assertThat(size(output), equalTo(2));
		assertThat(output, containsInAnyOrder(_first, _second));

		final ArgumentCaptor<FilterStore.Filter> captor = ArgumentCaptor.forClass(FilterStore.Filter.class);

		verify(userStore).getUser();
		verify(selectedGroup).getName();
		verify(store).getAllFilters(eq("a classname"), eq("a group"));
		verify(converter, times(2)).storeToLogic(captor.capture());
		verifyNoMoreInteractions(store, converter, userStore, authenticatedUser, privilegeContext, selectedGroup);

		assertThat(captor.getAllValues().get(0), equalTo(first));
		assertThat(captor.getAllValues().get(1), equalTo(second));
	}

	@Test
	public void defaultFilterIsSettedForSingleFilterAndSingleGroup() throws Exception {
		// given
		final FilterStore.Filter stored = mock(FilterStore.Filter.class);
		doReturn("a classname") //
				.when(stored).getClassName();
		doReturn(stored) //
				.when(store).fetchFilter(anyLong());
		final FilterStore.Filter firstAlreadyJoined = mock(FilterStore.Filter.class);
		final FilterStore.Filter secondAlreadyJoined = mock(FilterStore.Filter.class);
		doReturn(asList(firstAlreadyJoined, secondAlreadyJoined)) //
				.when(store).getAllFilters(anyString(), anyString());
		final Filter _first = mock(Filter.class);
		final Filter _second = mock(Filter.class);
		doReturn(_first).doReturn(_second) //
				.when(converter).storeToLogic(any(FilterStore.Filter.class));

		// when
		defaultFilterLogic.setDefault(asList(42L), asList("a group"));

		// then
		final ArgumentCaptor<Iterable> disjoinCaptor = ArgumentCaptor.forClass(Iterable.class);
		final ArgumentCaptor<Iterable> joinCaptor = ArgumentCaptor.forClass(Iterable.class);

		verify(store).fetchFilter(eq(42L));
		verify(store).getAllFilters(eq("a classname"), eq("a group"));
		verify(store).disjoin(eq("a group"), disjoinCaptor.capture());
		verify(store).join(eq("a group"), joinCaptor.capture());

		assertThat((Iterable<FilterStore.Filter>) disjoinCaptor.getValue(),
				containsInAnyOrder(firstAlreadyJoined, secondAlreadyJoined));
		assertThat((Iterable<FilterStore.Filter>) joinCaptor.getValue(), containsInAnyOrder(stored));
	}

	@Test
	public void defaultFilterIsSettedForMultipleFiltersAndMultipleGroups() throws Exception {
		// given
		final FilterStore.Filter stored = mock(FilterStore.Filter.class);
		doReturn("a classname") //
				.when(stored).getClassName();
		doReturn(stored) //
				.when(store).fetchFilter(anyLong());
		final FilterStore.Filter firstAlreadyJoined = mock(FilterStore.Filter.class);
		final FilterStore.Filter secondAlreadyJoined = mock(FilterStore.Filter.class);
		doReturn(asList(firstAlreadyJoined, secondAlreadyJoined)) //
				.when(store).getAllFilters(anyString(), anyString());
		final Filter _first = mock(Filter.class);
		final Filter _second = mock(Filter.class);
		doReturn(_first).doReturn(_second) //
				.when(converter).storeToLogic(any(FilterStore.Filter.class));

		// when
		defaultFilterLogic.setDefault(asList(1L, 2L), asList("foo", "bar"));

		// then
		final ArgumentCaptor<Long> filterCaptor = ArgumentCaptor.forClass(Long.class);
		final ArgumentCaptor<String> groupCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> groupCaptor2 = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> groupCaptor3 = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Iterable> disjoinCaptor = ArgumentCaptor.forClass(Iterable.class);
		final ArgumentCaptor<Iterable> joinCaptor = ArgumentCaptor.forClass(Iterable.class);

		verify(store, times(2)).fetchFilter(filterCaptor.capture());
		verify(store, times(4)).getAllFilters(eq("a classname"), groupCaptor.capture());
		verify(store, times(4)).disjoin(groupCaptor2.capture(), disjoinCaptor.capture());
		verify(store, times(4)).join(groupCaptor3.capture(), joinCaptor.capture());

		assertThat(filterCaptor.getAllValues(), containsInAnyOrder(1L, 2L));
		assertThat(groupCaptor.getAllValues().get(0), equalTo("foo"));
		assertThat(groupCaptor.getAllValues().get(1), equalTo("bar"));
		assertThat(groupCaptor.getAllValues().get(2), equalTo("foo"));
		assertThat(groupCaptor.getAllValues().get(3), equalTo("bar"));

		assertThat(groupCaptor2.getAllValues().get(0), equalTo("foo"));
		assertThat(groupCaptor2.getAllValues().get(1), equalTo("bar"));
		assertThat(groupCaptor2.getAllValues().get(2), equalTo("foo"));
		assertThat(groupCaptor2.getAllValues().get(3), equalTo("bar"));

		assertThat((Iterable<FilterStore.Filter>) disjoinCaptor.getValue(),
				containsInAnyOrder(firstAlreadyJoined, secondAlreadyJoined));

		assertThat(groupCaptor3.getAllValues().get(0), equalTo("foo"));
		assertThat(groupCaptor3.getAllValues().get(1), equalTo("bar"));
		assertThat(groupCaptor3.getAllValues().get(2), equalTo("foo"));
		assertThat(groupCaptor3.getAllValues().get(3), equalTo("bar"));

		assertThat((Iterable<FilterStore.Filter>) joinCaptor.getValue(), containsInAnyOrder(stored));
	}

	@Test
	public void getGroupsWhichTheSpecifiedFilterIsDefault() throws Exception {
		// given
		doReturn(asList("foo", "bar", "baz")) //
				.when(store).joined(anyLong());

		// when
		final Iterable<String> output = defaultFilterLogic.getGroups(42L);

		// then
		assertThat(output, containsInAnyOrder("foo", "bar", "baz"));

		verify(store).joined(eq(42L));
	}

}
