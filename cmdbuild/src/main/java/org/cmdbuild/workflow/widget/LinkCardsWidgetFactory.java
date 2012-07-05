package org.cmdbuild.workflow.widget;

import java.util.List;
import java.util.Map;

import org.cmdbuild.model.widget.LinkCards;
import org.cmdbuild.model.widget.Widget;

public class LinkCardsWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "linkCards";

	private static final String FILTER = "Filter";
	private static final String CLASS_NAME = "ClassName";
	private static final String DEFAULT_SELECTION = "DefaultSelection";
	private static final String READ_ONLY = "NoSelect";
	private static final String SINGLE_SELECT = "SingleSelect";
	private static final String ALLOW_CARD_EDITING = "AllowCardEditing";
	private static final String WITH_MAP = "Map";
	private static final String MAP_LATITUDE = "StartMapWithLatitude";
	private static final String MAP_LONGITUDE = "StartMapWithLongitude";
	private static final String MAP_ZOOM = "StartMapWithZoom";
	private static final String REQUIRED = "Required";
	private static final String LABEL = "ButtonLabel";

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(Map<String, String> valueMap) {
		LinkCards widget = new LinkCards();

		setFilterAndClassName(valueMap, widget);
		setOutputName(valueMap, widget);
		widget.setDefaultSelection(valueMap.get(DEFAULT_SELECTION));
		widget.setReadOnly(readBoolean(valueMap.get(READ_ONLY)));
		widget.setSingleSelect(readBoolean(valueMap.get(SINGLE_SELECT)));
		widget.setAllowCardEditing(readBoolean(valueMap.get(ALLOW_CARD_EDITING)));
		widget.setEnableMap(readBoolean(valueMap.get(WITH_MAP)));
		widget.setMapLatitude(readInteger(valueMap.get(MAP_LATITUDE)));
		widget.setMapLongitude(readInteger(valueMap.get(MAP_LONGITUDE)));
		widget.setMapZoom(readInteger(valueMap.get(MAP_ZOOM)));
		widget.setRequired(readBoolean(valueMap.get(REQUIRED)));
		widget.setTemplates(extractUnmanagedParameters(valueMap,
			FILTER, CLASS_NAME, DEFAULT_SELECTION, READ_ONLY, 
			SINGLE_SELECT, ALLOW_CARD_EDITING, WITH_MAP, MAP_LATITUDE,
			MAP_LONGITUDE, MAP_ZOOM, REQUIRED, LABEL));

		return widget;
	}

	/*
	 * If the filter is set the given ClassName is ignored
	 * and is used the filter
	 */
	private void setFilterAndClassName(Map<String, String> valueMap,
			LinkCards widget) {
		final String filter = valueMap.get(FILTER);
		if (filter != null) {
			widget.setFilter(filter);
			widget.setClassName(readClassNameFromCQLFilter(filter));
		} else {
			widget.setClassName(valueMap.get(CLASS_NAME));
		}
	}

	/*
	 * The outputName is set as key with null value
	 * in the XPDL definition. If there are some of
	 * this kind of value, take the first as outputName
	 */
	private void setOutputName(Map<String, String> valueMap, LinkCards widget) {
		List<String> outputs = extractParametersWithNullAsValue(valueMap);
		if (outputs.size() > 0) {
			widget.setOutputName(outputs.get(0));
		}
	}
}
