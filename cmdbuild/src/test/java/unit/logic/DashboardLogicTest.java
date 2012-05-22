package unit.logic;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.model.dashboard.ChartDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition.DashboardColumn;
import org.cmdbuild.services.store.DashboardStore;
import org.junit.Before;
import org.junit.Test;

public class DashboardLogicTest {
	private static DashboardLogic logic;
	private static DashboardStore store;

	@Before
	public void setUp() {
		store = mock(DashboardStore.class);
		logic = new DashboardLogic(null, store);
	}

	@SuppressWarnings("serial")
	@Test
	public void addDashboard() {
		final DashboardDefinition dd = new DashboardDefinition();

		when(store.add(dd)).thenReturn(new Long(11));

		Long newDashboardId = logic.add(dd);

		verify(store).add(dd);
		assertEquals(new Long(11), newDashboardId);

		final DashboardDefinition dd2 = new DashboardDefinition();
		dd2.setColumns(new ArrayList<DashboardColumn>() {{
			add(new DashboardColumn());
			add(new DashboardColumn());
		}});

		try {
			logic.add(dd2);
			fail("Could not add a dashboard with columns");
		} catch (IllegalArgumentException e) {
			String expectedMsg = DashboardLogic.errors.initDashboardWithColumns();
			assertEquals(expectedMsg, e.getMessage());
		}

		final DashboardDefinition dd3 = new DashboardDefinition();
		dd3.addChart("aChart", new ChartDefinition());

		try {
			logic.add(dd3);
			fail("Could not add a dashboard with charts");
		} catch (IllegalArgumentException e) {
			String expectedMsg = DashboardLogic.errors.initDashboardWithColumns();
			assertEquals(expectedMsg, e.getMessage());
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void modifyDashboard() {
		final DashboardDefinition dashboard = mock(DashboardDefinition.class);
		final Long dashboardId = new Long(123);
		final String name = "name", description = "description";
		final ArrayList<Integer> groups = new ArrayList<Integer>(){{ add(1); }};

		final DashboardDefinition changes = new DashboardDefinition() {{
			setName(name);
			setDescription(description);
			setGroups(groups);
		}};

		when(store.get(dashboardId)).thenReturn(dashboard);

		logic.modifyBaseProperties(dashboardId, changes);

		verify(dashboard).setName(name);
		verify(dashboard).setDescription(description);
		verify(dashboard).setGroups(groups);
		verify(store).modify(dashboardId, dashboard);

		when(store.get(dashboardId)).thenReturn(null);

		try {
			logic.modifyBaseProperties(dashboardId, changes);
			fail("could not modify a dashboard if it is not stored");
		} catch (IllegalArgumentException e) {
			String expectedMsg = DashboardLogic.errors.undefinedDashboard(dashboardId);
			assertEquals(expectedMsg, e.getMessage());
		}
	}

	@Test
	public void removeDashboard() {
		final Long ddId = new Long(11);

		logic.remove(ddId);

		verify(store).remove(ddId);
	}

	@Test
	public void listDashboard() {
		logic.listDashboards();
		verify(store).list();
	}

	@Test
	public void addChart() {
		final Long dashboardId = new Long(11);
		final ChartDefinition chart= new ChartDefinition();
		final DashboardDefinition dashboard = mock(DashboardDefinition.class);

		when(store.get(dashboardId)).thenReturn(dashboard);

		String chartId = logic.addChart(dashboardId, chart);

		verify(store).get(dashboardId);
		verify(dashboard).addChart(chartId, chart);
		verify(store).modify(dashboardId, dashboard);
	}

	@Test
	public void removeChart() {
		final Long dashboardId = new Long(11);
		final String chartId = "a_unique_id";
		final DashboardDefinition dashboard = mock(DashboardDefinition.class);

		when(store.get(dashboardId)).thenReturn(dashboard);

		logic.removeChart(dashboardId, chartId);

		verify(store).get(dashboardId);
		verify(dashboard).popChart(chartId);
		verify(store).modify(dashboardId, dashboard);
	}

	@Test
	public void modifyChart() {
		final Long dashboardId = new Long(11);
		final String chartId = "a_unique_id";
		final ChartDefinition chart = mock(ChartDefinition.class);
		final DashboardDefinition dashboard = mock(DashboardDefinition.class);

		when(store.get(dashboardId)).thenReturn(dashboard);

		logic.modifyChart(dashboardId, chartId, chart);

		verify(store).get(dashboardId);
		verify(dashboard).modifyChart(chartId, chart);
		verify(store).modify(dashboardId, dashboard);
	}

	@Test
	public void moveChart() {
		final Long toDashboardId = new Long(11);
		final Long fromDashboardId = new Long(12);
		final String chartId = "a_unique_id";
		final ChartDefinition removedChart = mock(ChartDefinition.class);
		final DashboardDefinition toDashboard = mock(DashboardDefinition.class);
		final DashboardDefinition fromDashboard = mock(DashboardDefinition.class);

		when(store.get(toDashboardId)).thenReturn(toDashboard);
		when(store.get(fromDashboardId)).thenReturn(fromDashboard);
		when(fromDashboard.popChart(chartId)).thenReturn(removedChart);

		logic.moveChart(chartId, fromDashboardId, toDashboardId);

		verify(store).get(fromDashboardId);
		verify(store).get(toDashboardId);

		verify(fromDashboard).popChart(chartId);
		verify(toDashboard).addChart(chartId, removedChart);
		verify(store).modify(toDashboardId, toDashboard);
		verify(store).modify(fromDashboardId, fromDashboard);
	}

	@Test
	public void setColumns() {
		final Long dashboardId = new Long(12);
		final DashboardDefinition dashboard = mock(DashboardDefinition.class);
		final ArrayList<DashboardColumn> columns = new ArrayList<DashboardColumn>();

		when(store.get(dashboardId)).thenReturn(dashboard);

		logic.setColumns(dashboardId, columns);

		verify(store).get(dashboardId);
		verify(dashboard).setColumns(columns);
		verify(store).modify(dashboardId, dashboard);
	}
}