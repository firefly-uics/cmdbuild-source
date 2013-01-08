package integration.services.store;

import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.services.store.DataViewFilterStore;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.FilterStore.Filter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

public class DataViewFilterStoreTest extends IntegrationTestBase {

	private static final long USER_ID = 123L;
	private static final long ANOTHER_USER_ID = 456L;

	private DataViewFilterStore filterStore;

	@Before
	public void createFilterStore() throws Exception {
		filterStore = new DataViewFilterStore(dbDataView(), operationUser(USER_ID));
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
		filterStore.save(filter(null, "bar"));

		// then
	}

	@Test(expected = IllegalArgumentException.class)
	public void filterNameCannotBeEmpty() throws Exception {
		// given

		// when
		filterStore.save(filter("", "bar"));

		// then
	}

	@Test(expected = IllegalArgumentException.class)
	public void filterNameCannotBeBlank() throws Exception {
		// given

		// when
		filterStore.save(filter(" \t", "bar"));

		// then
	}

	@Test
	public void filterSavedAndRead() throws Exception {
		// given
		filterStore.save(filter("foo", "bar"));

		// when
		final Iterable<FilterStore.Filter> filters = filterStore.getAllFilters();

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(filter("foo", "bar")));
	}

	@Test
	public void filterModified() throws Exception {
		// given
		filterStore.save(filter("foo", "bar"));

		// when
		Iterable<FilterStore.Filter> filters = filterStore.getAllFilters();

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(filter("foo", "bar")));

		// but
		filterStore.save(filter("foo", "baz"));

		// when
		filters = filterStore.getAllFilters();

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(filter("foo", "baz")));
	}

	@Test
	public void filterSavedAndReadByUserId() throws Exception {
		// given
		filterStore.save(filter("bar", "baz"));
		final DataViewFilterStore anotherFilterStore = new DataViewFilterStore( //
				dbDataView(), operationUser(ANOTHER_USER_ID));
		anotherFilterStore.save(filter("foo", "baz"));

		// when
		final Iterable<FilterStore.Filter> filters = filterStore.getAllFilters();

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(filter("bar", "baz")));
	}

	@Test
	public void filterDataFullyRead() throws Exception {
		// given
		filterStore.save(filter("foo", "bar", "baz"));

		// when
		final Filter filter = filterStore.getAllFilters().iterator().next();

		// then
		assertThat(filter.getName(), equalTo("foo"));
		assertThat(filter.getDescription(), equalTo("bar"));
		assertThat(filter.getValue(), equalTo("baz"));
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

	private Filter filter(final String name, final String value) {
		return filter(name, name, value);
	}

	private Filter filter(final String name, final String description, final String value) {
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

		};
	}

}
