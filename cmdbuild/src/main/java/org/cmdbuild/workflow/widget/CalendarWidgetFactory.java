package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.cmdbuild.model.widget.Calendar;
import org.cmdbuild.model.widget.Widget;

public class CalendarWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "calendar";
	private static final String TARGET_CLASS = "ClassName";
	private static final String CQL_FILTER = "Filter";
	private static final String TITLE = "EventTitle";
	private static final String START_DATE = "EventStartDate";
	private static final String END_DATE = "EventEndDate";
	private static final String DEFAULT_DATE = "DefaultDate";

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(Map<String, Object> valueMap) {
		final Calendar widget = new Calendar();

		final String filter = readString(valueMap.get(CQL_FILTER));
		if (filter != null) {
			widget.setFilter(filter);
			widget.setTargetClass(readClassNameFromCQLFilter(filter));
		} else {
			widget.setTargetClass(readString(valueMap.get(TARGET_CLASS)));
		}

		widget.setEventTitle(readString(valueMap.get(TITLE)));
		widget.setStartDate(readString(valueMap.get(START_DATE)));
		widget.setEndDate(readString(valueMap.get(END_DATE)));
		widget.setDefaultDate(readString(valueMap.get(DEFAULT_DATE)));

		return widget;
	}

}
