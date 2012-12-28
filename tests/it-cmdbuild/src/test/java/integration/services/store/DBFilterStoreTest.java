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
import org.cmdbuild.services.store.FilterStore.Filter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

public class DBFilterStoreTest extends IntegrationTestBase {

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
		assertThat(size(filterStore.getAllFilters()), equalTo(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void filterNameCannotBeNull() throws Exception {
		filterStore.save(filter(null, "bar"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void filterNameCannotBeEmpty() throws Exception {
		filterStore.save(filter("", "bar"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void filterNameCannotBeBlank() throws Exception {
		filterStore.save(filter(" \t", "bar"));
	}

	@Test
	public void filterSavedAndRead() throws Exception {
		filterStore.save(filter("foo", "bar"));
		assertThat(size(filterStore.getAllFilters()), equalTo(1));
		assertThat(filterStore.getAllFilters(), contains(filter("foo", "bar")));
	}

	@Test
	public void filterSavedAndReadByUserId() throws Exception {
		final DataViewFilterStore anotherFilterStore = new DataViewFilterStore( //
				dbDataView(), operationUser(ANOTHER_USER_ID));
		anotherFilterStore.save(filter("foo", "baz"));
		filterStore.save(filter("bar", "baz"));
		assertThat(size(filterStore.getAllFilters()), equalTo(1));
		assertThat(filterStore.getAllFilters(), contains(filter("bar", "baz")));
	}

	@Test
	public void filterDataFullyRead() throws Exception {
		filterStore.save(filter("foo", "bar", "baz"));
		final Filter filter = filterStore.getAllFilters().iterator().next();
		assertThat(filter.getName(), equalTo("foo"));
		assertThat(filter.getDescription(), equalTo("bar"));
		assertThat(filter.getValue(), equalTo("baz"));
	}

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
