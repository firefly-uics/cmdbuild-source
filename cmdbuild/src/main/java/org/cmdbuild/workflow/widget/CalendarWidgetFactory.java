package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.cmdbuild.model.widget.Calendar;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.TemplateRepository;

public class CalendarWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "calendar";

	public static final String TARGET_CLASS = "ClassName";
	public static final String CQL_FILTER = "Filter";
	public static final String TITLE = "EventTitle";
	public static final String START_DATE = "EventStartDate";
	public static final String END_DATE = "EventEndDate";
	public static final String DEFAULT_DATE = "DefaultDate";

	public CalendarWidgetFactory(final TemplateRepository templateRespository) {
		super(templateRespository);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
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
