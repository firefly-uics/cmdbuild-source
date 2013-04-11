package unit.privileges.fetchers;

import static org.cmdbuild.spring.SpringIntegrationUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.privileges.fetchers.CMClassPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.ViewPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.factories.CMClassPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.FilterPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.PrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.ViewPrivilegeFetcherFactory;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class PrivilegeFetcherFactoriesTest {

	
	
	@Test
	public void shouldReturnTheCorrectNumberOfPrivilegeFetcherFactories() {
		// when
		Iterable<PrivilegeFetcherFactory> factories ;
		factories= applicationContext().getBean("privilegeFetcherFactories", List.class);
		final int factoriesSize = Iterables.size(factories);

		// then
		assertEquals(factoriesSize, 3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfGroupIsNotSetForCMClass() {
		// given
		final CMClassPrivilegeFetcherFactory classFactory = new CMClassPrivilegeFetcherFactory(mockDataView());

		// when
		classFactory.create();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfGroupIsNotSetForView() {
		// given
		final ViewPrivilegeFetcherFactory viewFactory = new ViewPrivilegeFetcherFactory(mockDataView());

		// when
		viewFactory.create();
	}

	@Test
	public void eachFactoryShouldReturnTheCorrectTypeOfPrivilegeFetcher() {
		// given
		final CMClassPrivilegeFetcherFactory classFactory = new CMClassPrivilegeFetcherFactory(mockDataView());
		final ViewPrivilegeFetcherFactory viewFactory = new ViewPrivilegeFetcherFactory(mockDataView());
		classFactory.setGroupId(1L);
		viewFactory.setGroupId(1L);

		// when
		final PrivilegeFetcher classPrivilegeFetcher = classFactory.create();
		final PrivilegeFetcher viewPrivilegeFetcher = viewFactory.create();

		// then
		assertTrue(classPrivilegeFetcher instanceof CMClassPrivilegeFetcher);
		assertTrue(viewPrivilegeFetcher instanceof ViewPrivilegeFetcher);

	}

	private static DBDataView mockDataView() {
		return mock(DBDataView.class);
	}

}
