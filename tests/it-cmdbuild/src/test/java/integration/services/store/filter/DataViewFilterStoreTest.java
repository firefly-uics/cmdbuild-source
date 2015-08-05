package integration.services.store.filter;

import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.services.store.filter.DataViewFilterStore;
import org.cmdbuild.services.store.filter.FilterConverter;
import org.cmdbuild.services.store.filter.FilterDTO;
import org.cmdbuild.services.store.filter.FilterStore;
import org.cmdbuild.services.store.filter.FilterStore.Filter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

import com.google.common.collect.Iterables;

public class DataViewFilterStoreTest extends IntegrationTestBase {

	private static final long USER_ID = 123L;
	private static final long ANOTHER_USER_ID = 456L;

	private DataViewFilterStore filterStore;
	private CMClass roleClass;
	private CMClass userClass;
	private final Long EMPTY_ID = null;

	@Before
	public void createFilterStore() throws Exception {
		filterStore = new DataViewFilterStore(dbDataView(), operationUser(USER_ID), new FilterConverter(dbDataView()));
		roleClass = dbDataView().findClass("Role");
		userClass = dbDataView().findClass("User");
	}

	@After
	public void clearSystemTables() throws Exception {
		dbDataView().clear(filterStore.getFilterClass());
	}

	@Test
	public void noFiltersDefinedAsDefault() throws Exception {
		// given

		// when
		final Iterable<FilterStore.Filter> filters = filterStore.getAllUserFilters();

		// then
		assertThat(size(filters), equalTo(0));
	}

	@Test
	public void shouldFetchOnlyUserFilters() throws Exception {
		// given
		final Long id = filterStore
				.create(userFilter("foo", "bar", roleClass.getIdentifier().getLocalName(), EMPTY_ID));
		filterStore.create(groupFilter("group_filter", "value", roleClass.getIdentifier().getLocalName(), EMPTY_ID));

		// when
		final Iterable<FilterStore.Filter> filters = filterStore.getAllUserFilters();

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(userFilter("foo", "bar", roleClass.getIdentifier().getLocalName(), id)));
	}

	@Test
	public void filterModified() throws Exception {
		// given
		final Long createdId = filterStore.create(userFilter("foo", "bar", roleClass.getIdentifier().getLocalName(),
				EMPTY_ID));

		// when
		Iterable<FilterStore.Filter> filters = filterStore.getAllUserFilters();

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(userFilter("foo", "bar", roleClass.getIdentifier().getLocalName(), createdId)));

		// but
		filterStore.update(userFilter("foo", "baz", roleClass.getIdentifier().getLocalName(), createdId));

		// when
		filters = filterStore.getAllUserFilters();

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(userFilter("foo", "baz", roleClass.getIdentifier().getLocalName(), createdId)));
	}

	@Test
	public void filterSavedAndReadByUserIdAndClassName() throws Exception {
		// given
		final Long id = filterStore
				.create(userFilter("bar", "baz", roleClass.getIdentifier().getLocalName(), EMPTY_ID));
		final DataViewFilterStore anotherFilterStore = new DataViewFilterStore( //
				dbDataView(), operationUser(ANOTHER_USER_ID), new FilterConverter(dbDataView()));
		anotherFilterStore.create(userFilter("foo", "baz", roleClass.getIdentifier().getLocalName(), EMPTY_ID));

		// when
		Iterable<FilterStore.Filter> filters = filterStore.getFiltersForCurrentlyLoggedUser(roleClass.getIdentifier()
				.getLocalName());

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(userFilter("bar", "baz", roleClass.getIdentifier().getLocalName(), id)));

		// but
		filters = filterStore.getFiltersForCurrentlyLoggedUser(userClass.getIdentifier().getLocalName());
		assertThat(size(filters), equalTo(0));
	}

	@Test
	public void filterDataFullyRead() throws Exception {
		// given
		filterStore.create(filter("foo", "bar", "baz", roleClass.getIdentifier().getLocalName(), EMPTY_ID, false));

		// when
		final Filter filter = filterStore.getAllUserFilters().iterator().next();

		// then
		assertThat(filter.getName(), equalTo("foo"));
		assertThat(filter.getDescription(), equalTo("bar"));
		assertThat(filter.getValue(), equalTo("baz"));
	}

	@Test
	public void userCanHaveMoreThanOneFilterWithSameNameButForDifferentEntryType() throws Exception {
		// given
		filterStore.create(userFilter("name", "value", roleClass.getIdentifier().getLocalName(), EMPTY_ID));
		filterStore.create(userFilter("name", "value2", userClass.getIdentifier().getLocalName(), EMPTY_ID));

		// when
		final Iterable<Filter> userFilters = filterStore.getAllUserFilters();

		// then
		assertThat(Iterables.size(userFilters), equalTo(2));
	}

	@Test(expected = Exception.class)
	public void userCanHaveOnlyOneFilterWithSameNameAndEntryType() throws Exception {
		// when
		filterStore.create(userFilter("name", "value", roleClass.getIdentifier().getLocalName(), EMPTY_ID));
		filterStore.create(userFilter("name", "value2", roleClass.getIdentifier().getLocalName(), EMPTY_ID));
	}

	@Test
	public void testPagination() throws Exception {
		// given
		filterStore.create(userFilter("foo1", "value1", roleClass.getIdentifier().getLocalName(), EMPTY_ID));
		filterStore.create(userFilter("foo2", "value2", roleClass.getIdentifier().getLocalName(), EMPTY_ID));
		filterStore.create(userFilter("foo3", "value3", roleClass.getIdentifier().getLocalName(), EMPTY_ID));
		filterStore.create(userFilter("foo4", "value4", roleClass.getIdentifier().getLocalName(), EMPTY_ID));

		// when
		final PagedElements<Filter> userFilters = filterStore.getAllUserFilters(roleClass.getIdentifier()
				.getLocalName(), 0, 2);

		// then
		assertEquals(4, userFilters.totalSize());
		assertEquals(2, Iterables.size(userFilters));
	}

	/*
	 * Utilities
	 */

	private OperationUser operationUser(final long id) {
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		when(authenticatedUser.getId()) //
				.thenReturn(id);
		return new OperationUser( //
				authenticatedUser, //
				mock(PrivilegeContext.class), //
				mock(CMGroup.class));
	}

	private Filter userFilter(final String name, final String value, final String className, final Long id) {
		return filter(name, name, value, className, id, false);
	}

	private Filter groupFilter(final String name, final String value, final String className, final Long id) {
		return filter(name, name, value, className, id, true);
	}

	private Filter filter(final String name, final String description, final String value, final String className,
			final Long id, final boolean asTemplate) {
		return FilterDTO.newFilter() //
				.withId(id) //
				.withName(name) //
				.withDescription(description) //
				.forClass(className) //
				.withValue(value) //
				.asTemplate(asTemplate) //
				.build();
	}

}
