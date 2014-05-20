package org.cmdbuild.model.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.cmdbuild.model.dashboard.DefaultDashboardDefinition.DashboardColumn;

public class ForwardingDashboardDefinition implements DashboardDefinition {

	private final DashboardDefinition delegate;
	private String description;
	
	public static DashboardDefinition of(DashboardDefinition delegate){
		return new ForwardingDashboardDefinition(delegate);
	}

	private ForwardingDashboardDefinition(DashboardDefinition delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public void setName(String name) {
		delegate.setName(name);

	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public LinkedHashMap<String, ChartDefinition> getCharts() {
		return delegate.getCharts();
	}

	@Override
	public void setCharts(LinkedHashMap<String, ChartDefinition> charts) {
		delegate.setCharts(charts);
	}

	@Override
	public ChartDefinition getChart(String chartId) {
		return delegate.getChart(chartId);
	}

	@Override
	public void addChart(String chartId, ChartDefinition chart) {
		delegate.addChart(chartId, chart);
	}

	@Override
	public void modifyChart(String chartId, ChartDefinition chart) {
		delegate.modifyChart(chartId, chart);
	}

	@Override
	public ChartDefinition popChart(String chartId) {
		return delegate.popChart(chartId);
	}

	@Override
	public ArrayList<DashboardColumn> getColumns() {
		return delegate.getColumns();
	}

	@Override
	public void setColumns(ArrayList<DashboardColumn> columns) {
		delegate.setColumns(columns);
	}

	@Override
	public void addColumn(DashboardColumn column) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeColumn(DashboardColumn column) {
		delegate.removeColumn(column);
	}

	@Override
	public ArrayList<String> getGroups() {
		return delegate.getGroups();
	}

	@Override
	public void setGroups(ArrayList<String> groups) {
		delegate.setGroups(groups);
	}

	@Override
	public void addGroup(String group) {
		delegate.addGroup(group);
	}

	@Override
	public void removeGroup(String group) {
		delegate.removeGroup(group);
	}

	@Override
	public String getDefaultDescription() {
		return delegate.getDescription();
	}

}
