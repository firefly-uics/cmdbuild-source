package org.cmdbuild.model.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DashboardDefinition {

	public static ErrorMessageBuilder errors = new ErrorMessageBuilder();

	private String	name,
					description;

	private LinkedHashMap<String, ChartDefinition> charts;
	private ArrayList<DashboardColumn> columns;
	private ArrayList<Integer> groups;

	public DashboardDefinition() {
		charts = new LinkedHashMap<String, ChartDefinition>();
		columns = new ArrayList<DashboardColumn>();
		groups = new ArrayList<Integer>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// charts
	public LinkedHashMap<String, ChartDefinition> getCharts() {
		return charts;
	}

	public void setCharts(LinkedHashMap<String, ChartDefinition> charts) {
		this.charts = charts;
	}

	public ChartDefinition getChart(String chartId) {
		ensureChartId(chartId);
		return this.charts.get(chartId);
	}

	public void addChart(String chartId, ChartDefinition chart) {
		if (!this.charts.containsKey(chartId)) {
			this.putChart(chartId, chart);
		} else {
			throw new IllegalArgumentException(errors.duplicateChartIdForDashboard(chartId, this.getName()));
		}
	}

	public void modifyChart(String chartId, ChartDefinition chart) {
		ensureChartId(chartId);
		this.putChart(chartId, chart);
	}

	public ChartDefinition popChart(String chartId) {
		ensureChartId(chartId);
		return this.charts.remove(chartId);
	}

	// columns
	public ArrayList<DashboardColumn> getColumns() {
		return columns;
	}

	/**
	 * Does not make checks here because this method
	 * is used only by Jackson to de/serialize the columns
	 * so we we have not control to the order of json parsing
	 * so it's possible that it try to set the columns first, and
	 * then add the charts
	 */
	public void setColumns(ArrayList<DashboardColumn> columns) {
		this.columns = columns;
	}

	public void addColumn(DashboardColumn column) {
		ensureChartsConsistency(column);
		this.columns.add(column);
	}

	public void removeColumn(DashboardColumn column) {
		this.columns.remove(column);
	}

	// groups
	public ArrayList<Integer> getGroups() {
		return groups;
	}

	public void setGroups(ArrayList<Integer> groups) {
		this.groups = groups;
	}

	public void addGroup(Integer group) {
		this.groups.add(group);
	}

	public void removeGroup(Integer group) {
		this.groups.remove(group);
	}

	/*
	 *  support function to check that a chart is not null, before
	 *  to add it in a dashboard
	 */
	private void putChart(String chartId, ChartDefinition chart) {
		if (chart != null) {
			this.charts.put(chartId, chart);
		} else {
			throw new IllegalArgumentException(errors.nullChart(this.getName()));
		}
	}

	/*
	 * support function to throw an exception if try to
	 * reach a chart that is not stored in the dashboard
	 */
	private void ensureChartId(String chartId) {
		if (!this.charts.containsKey(chartId)) {
			throw new IllegalArgumentException(errors.notFoundChartIdForDashboard(chartId, this.getName()));
		}
	}

	/*
	 * support function called when add a column to be sure that the
	 * charts referred in the column are stored in the dashboard
	 */
	private void ensureChartsConsistency(DashboardColumn column) {
		for (String chartId: column.getCharts()) {
			if (!this.charts.containsKey(chartId)) {
				throw new IllegalArgumentException(errors.wrongChartInColumn(chartId, this.getName()));
			}
		}
	}

	/*
	 * A representation of a column of the dashboard
	 * to manage the references to the charts
	 */
	public static class DashboardColumn {
		private float width;
		private ArrayList<String> charts;

		public DashboardColumn() {
			width = 0;
			charts = new ArrayList<String>();
		}

		public float getWidth() {
			return width;
		}

		public void setWidth(float width) {
			this.width = width;
		}

		public ArrayList<String> getCharts() {
			return charts;
		}

		public void setCharts(ArrayList<String> charts) {
			this.charts = charts;
		}

		public void addChart(String chartId) {
			this.charts.add(chartId);
		}

		public void removeChart(String chartId) {
			this.charts.remove(chartId);
		}
	}



	/* 
	 * to avoid an useless errors hierarchy
	 * define this object that build the errors messages
	 * These are used also in the tests to ensure
	 * that a right message is provided by the exception
	 */
	public static class ErrorMessageBuilder {
		public String duplicateChartIdForDashboard(String chartId, String dashboardName) {
			String errorFormat = "The chart id %s is already used on dashboard %s";
			return String.format(errorFormat, chartId, dashboardName);
		}

		public String notFoundChartIdForDashboard(String chartId, String dashboardName) {
			String errorFormat = "I'm not able to retrieve the chart with %s in the dashboard %s";
			return String.format(errorFormat, chartId, dashboardName);
		}

		public String nullChart(String dashboardName) {
			String errorFormat = "You are trying to add a null chart to dashboard %s";
			return String.format(errorFormat, dashboardName);
		}

		public String wrongChartInColumn(String chartId, String dashboardName) {
			String errorForma = "You are trying to add a column with the chart %s that is not stored " +
					"in dashboard %s";
			return String.format(errorForma, chartId, dashboardName);
		}
 	}
}