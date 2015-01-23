package unit.cxf;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Models.newProcessStatus;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.service.rest.cxf.CxfProcessesConfiguration;
import org.cmdbuild.service.rest.model.ProcessStatus;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.workflow.LookupHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class CxfProcessesConfigurationTest {

	private LookupHelper lookupHelper;

	private CxfProcessesConfiguration cxfAttachmentsConfiguration;

	@Before
	public void setUp() throws Exception {
		lookupHelper = mock(LookupHelper.class);
		cxfAttachmentsConfiguration = new CxfProcessesConfiguration(lookupHelper);
	}

	@Test
	public void logicCalledWhenCategoriesAreRead() throws Exception {
		// given
		final Lookup first = Lookup.newInstance().withId(123L).withDescription("foo").build();
		final Lookup second = Lookup.newInstance().withId(456L).withDescription("bar").build();
		doReturn(asList(first, second)) //
				.when(lookupHelper).allLookups();

		// when
		final ResponseMultiple<ProcessStatus> response = cxfAttachmentsConfiguration.readStatuses();

		// then
		final InOrder inOrder = inOrder(lookupHelper);
		inOrder.verify(lookupHelper).allLookups();
		inOrder.verifyNoMoreInteractions();

		assertThat(response.getMetadata().getTotal(), equalTo(2L));
		assertThat(size(response.getElements()), equalTo(2));
		assertThat(get(response.getElements(), 0), equalTo(newProcessStatus() //
				.withId(123L) //
				.withDescription("foo") //
				.build()));
		assertThat(get(response.getElements(), 1), equalTo(newProcessStatus() //
				.withId(456L) //
				.withDescription("bar") //
				.build()));
	}

}
