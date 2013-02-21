package integration.services.store;

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
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.services.store.DataViewFilterStore;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.FilterStore.Filter;
import org.cmdbuild.services.store.FilterStore.GetFiltersResponse;
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

	@Before
	public void createFilterStore() throws Exception {
		filterStore = new DataViewFilterStore(dbDataView(), operationUser(USER_ID));
		roleClass = dbDataView().findClass("Role");
		userClass = dbDataView().findClass("User");
	}

	@After
	public void clearSystemTables() throws Exception {
		dbDataView().clear(DBClass.class.cast(filterStore.getFilterClass()));
	}

	@Test
	public void noFiltersDefinedAsDefault() throws Exception {
		// given

		// when
		final Iterable<FilterStore.Filter> filters = filterStore.getAllFilters();

		// then
		assertThat(size(filters), equalTo(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void filterNameCannotBeNull() throws Exception {
		// given

		// when
		filterStore.save(filter(null, "bar", roleClass.getName(), ""));

		// then
	}

	@Test(expected = IllegalArgumentException.class)
	public void filterNameCannotBeEmpty() throws Exception {
		// given

		// when
		filterStore.save(filter("", "bar", roleClass.getName(), ""));

		// then
	}

	@Test(expected = IllegalArgumentException.class)
	public void filterNameCannotBeBlank() throws Exception {
		// given

		// when
		filterStore.save(filter(" \t", "bar", roleClass.getName(), ""));

		// then
	}

	@Test
	public void filterSavedAndRead() throws Exception {
		// given
		filterStore.save(filter("foo", "bar", roleClass.getName(), ""));

		// when
		final Iterable<FilterStore.Filter> filters = filterStore.getAllFilters();

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(filter("foo", "bar", roleClass.getName(), "")));
	}

	@Test
	public void filterModified() throws Exception {
		// given
		filterStore.save(filter("foo", "bar", roleClass.getName(), ""));

		// when
		Iterable<FilterStore.Filter> filters = filterStore.getAllFilters();

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(filter("foo", "bar", roleClass.getName(), "")));

		// but
		filterStore.save(filter("foo", "baz", roleClass.getName(), ""));

		// when
		filters = filterStore.getAllFilters();

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(filter("foo", "baz", roleClass.getName(), "")));
	}

	@Test
	public void filterSavedAndReadByUserIdAndClassName() throws Exception {
		// given
		filterStore.save(filter("bar", "baz", roleClass.getName(), ""));
		final DataViewFilterStore anotherFilterStore = new DataViewFilterStore( //
				dbDataView(), operationUser(ANOTHER_USER_ID));
		anotherFilterStore.save(filter("foo", "baz", roleClass.getName(), ""));

		// when
		Iterable<FilterStore.Filter> filters = filterStore.getUserFilters(roleClass.getName());

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(filter("bar", "baz", roleClass.getName(), "")));

		// but
		filters = filterStore.getUserFilters(userClass.getName());
		assertThat(size(filters), equalTo(0));
	}

	@Test
	public void filterDataFullyRead() throws Exception {
		// given
		filterStore.save(filter("foo", "bar", "baz", roleClass.getName()));

		// when
		final Filter filter = filterStore.getAllFilters().iterator().next();

		// then
		assertThat(filter.getName(), equalTo("foo"));
		assertThat(filter.getDescription(), equalTo("bar"));
		assertThat(filter.getValue(), equalTo("baz"));
	}

	@Test
	public void userCanHaveMoreThanOneFilterWithSameNameButForDifferentEntryType() throws Exception {
		// given
		filterStore.save(filter("name", "description", "value", roleClass.getName()));
		filterStore.save(filter("name", "desc", "value2", userClass.getName()));

		// when
		final Iterable<Filter> userFilters = filterStore.getAllFilters();

		// then
		assertThat(Iterables.size(userFilters), equalTo(2));
	}

	@Test
	public void userCanHaveOnlyOneFilterWithSameNameAndEntryType() throws Exception {
		// given
		filterStore.save(filter("name", "description", "value", roleClass.getName()));
		filterStore.save(filter("name", "desc", "value2", roleClass.getName()));

		// when
		final Iterable<Filter> userFilters = filterStore.getAllFilters();

		// then
		assertThat(Iterables.size(userFilters), equalTo(1));
	}

	@Test
	public void testPagination() throws Exception {
		// given
		filterStore.save(filter("foo1", "description1", "value1", roleClass.getName()));
		filterStore.save(filter("foo2", "description2", "value2", roleClass.getName()));
		filterStore.save(filter("foo3", "description3", "value3", roleClass.getName()));
		filterStore.save(filter("foo4", "description4", "value4", roleClass.getName()));

		// when
		final GetFiltersResponse userFilters = filterStore.getAllFilters(0, 2);

		// then
		assertEquals(4, userFilters.count());
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

	private Filter filter(final String name, final String value, final String className, final String id) {
		return filter(name, name, value, className, id);
	}

	// But, a mock instead an in-line implementation?
	private Filter filter(final String name, final String description, final String value, final String className, final String id) {
		return new Filter() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getDescription() {
				return description;
			}

			@Override
			public String getValue() {
				return value;
			}

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public boolean equals(final Object obj) {
				if (obj == this) {
					return true;
				}
				if (!(obj instanceof Filter)) {
					return false;
				}
				final Filter filter = Filter.class.cast(obj);
				return this.getName().equals(filter.getName()) //
						&& this.getValue().equals(filter.getValue());
			}

			@Override
			public String toString() {
				return getValue();
			}

			@Override
			public String getId() {
				return id;
			}

		};
	}

}
