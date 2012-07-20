package org.cmdbuild.model.widget;

import java.util.Map;

public class LinkCards extends Widget {

	/**
	 * A CQL query to fill the linkCard grid
	 * Use it or the className
	 */
	private String filter;

	/**
	 * Fill the linkCard grid with the cards
	 * of this class. Use it or the filter
	 */
	private String className;

	/**
	 * A CQL query to define the starting
	 * selection
	 */
	private String defaultSelection;

	/**
	 * If true, the grid is in read-only mode
	 * so you can not select its rows
	 */
	private boolean readOnly;

	/**
	 * To allow the selection of only a row
	 */
	private boolean singleSelect;

	/**
	 * Add an icon at the right of each row
	 * to edit the referred card
	 */
	private boolean allowCardEditing;

	/**
	 * If true, the user must select a card
	 * on this widget before to can advance
	 * with the process
	 */
	private boolean required;

	/**
	 * If true, enable the map module for
	 * this widget
	 */
	private boolean enableMap;

	/**
	 * The latitude to use as
	 * default for the map module
	 */
	private Integer mapLatitude;

	/**
	 * The longitude to use as
	 * default for the map module
	 */
	private Integer mapLongitude;

	/**
	 * The zoom level to use as
	 * default for the map module
	 */
	private Integer mapZoom;

	/**
	 * The name of the variable in which
	 * find the selections of the widget
	 * during the save operation
	 */
	private String outputName;

	/**
	 * Templates to use for the CQL filters
	 */
	private Map<String, String> templates;

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDefaultSelection() {
		return defaultSelection;
	}

	public void setDefaultSelection(String defaultSelection) {
		this.defaultSelection = defaultSelection;
	}

	public boolean isSingleSelect() {
		return singleSelect;
	}

	public void setSingleSelect(boolean singleSelect) {
		this.singleSelect = singleSelect;
	}

	public boolean isAllowCardEditing() {
		return allowCardEditing;
	}

	public void setAllowCardEditing(boolean allowCardEditing) {
		this.allowCardEditing = allowCardEditing;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isEnableMap() {
		return enableMap;
	}

	public void setEnableMap(boolean enableMap) {
		this.enableMap = enableMap;
	}

	public Integer getMapLatitude() {
		return mapLatitude;
	}

	public void setMapLatitude(Integer mapLatitude) {
		this.mapLatitude = mapLatitude;
	}

	public Integer getMapLongitude() {
		return mapLongitude;
	}

	public void setMapLongitude(Integer mapLongitude) {
		this.mapLongitude = mapLongitude;
	}

	public Integer getMapZoom() {
		return mapZoom;
	}

	public void setMapZoom(Integer mapZoom) {
		this.mapZoom = mapZoom;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	public Map<String, String> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, String> templates) {
		this.templates = templates;
	}
}
