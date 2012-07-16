package unit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.services.soap.MenuSchema;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.api.CachedWsSchemaApi;
import org.cmdbuild.workflow.api.SchemaApi;
import org.cmdbuild.workflow.api.SchemaApi.ClassInfo;
import org.cmdbuild.workflow.api.SharkWorkflowApi;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that the web service implementation of {@link SharkWorkflowApi} calls
 * the SOAP proxy correctly.
 */
public class CachedWsSchemaApiTest {

	private static final String CLASS1_NAME = "foo";
	private static final int CLASS1_ID = 101;
	private static final String CLASS2_NAME = "bar";
	private static final int CLASS2_ID = 102;
	private static final String PROCESS1_NAME = "baz";
	private static final int PROCESS1_ID = 103;

	private Private proxy;

	private SchemaApi api;

	@Before
	public void setUp() throws Exception {
		proxy = mock(Private.class);

		this.api = new CachedWsSchemaApi(proxy);
	}

	@Test
	public void classInformationsAreRetrievedAndCached() {
		ClassInfo classInfo;
		when(proxy.getCardMenuSchema()).thenReturn(
				menuItem(CLASS1_NAME, CLASS1_ID, //
						menuItem(CLASS2_NAME, CLASS2_ID) //
				)
		);
		when(proxy.getActivityMenuSchema()).thenReturn(
				menuItem(PROCESS1_NAME, PROCESS1_ID) //
		);

		classInfo = api.findClass(CLASS1_NAME);

		assertThat(classInfo.getId(), equalTo(CLASS1_ID));
		assertThat(classInfo.getName(), equalTo(CLASS1_NAME));
		assertThat(api.findClass(CLASS1_ID), is(sameInstance(classInfo)));

		classInfo = api.findClass(CLASS2_NAME);

		assertThat(classInfo.getId(), equalTo(CLASS2_ID));
		assertThat(classInfo.getName(), equalTo(CLASS2_NAME));

		classInfo = api.findClass(PROCESS1_NAME);

		assertThat(classInfo.getId(), equalTo(PROCESS1_ID));
		assertThat(classInfo.getName(), equalTo(PROCESS1_NAME));

		verify(proxy, times(1)).getCardMenuSchema();
		verify(proxy, times(1)).getActivityMenuSchema();
		verifyNoMoreInteractions(proxy);
	}

	@Test
	public void classInformationsAreRetrievedOnCacheMiss() {
		when(proxy.getCardMenuSchema()).thenReturn(
				menuItem(CLASS1_NAME, CLASS1_ID) //
		).thenReturn(
				menuItem(CLASS2_NAME, CLASS2_ID) //
		);
		when(proxy.getActivityMenuSchema()).thenReturn(
				menuItem(PROCESS1_NAME, PROCESS1_ID) //
		);

		assertThat(api.findClass(CLASS1_NAME), not(nullValue()));

		verify(proxy, times(1)).getCardMenuSchema();
		verify(proxy, times(1)).getActivityMenuSchema();

		assertThat(api.findClass(CLASS2_ID), not(nullValue()));

		verify(proxy, times(2)).getCardMenuSchema();
		verify(proxy, times(2)).getActivityMenuSchema();

		verifyNoMoreInteractions(proxy);
	}

	/*
	 * Utils
	 */

	private MenuSchema menuItem(final String name, final int id, final MenuSchema... children) {
		final MenuSchema menuItem = new MenuSchema();
		menuItem.setClassname(name);
		menuItem.setId(id);
		for (MenuSchema child : children) {
			menuItem.getChildren().add(child);
		}
		return menuItem;
	}
}
