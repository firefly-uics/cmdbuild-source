package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.model.widget.OpenReport;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.TemplateRepository;

public class OpenReportWidgetFactory extends ValuePairWidgetFactory {

	private static final String BUTTON_LABEL = "ButtonLabel";
	private static final String WIDGET_NAME = "createReport";
	private static final String REPORT_CODE = "ReportCode";
	private static final String FORCE_PDF = "ForcePDF";
	private static final String FORCE_CSV = "ForceCSV";

	// TODO: use these when implementing save
	private static final String SAVE_TO_ALFRESCO = "StoreInAlfresco"; // hahahah
	private static final String STORE_IN_PROCESS = "StoreInProcess";

	// TODO: if a day we'll use multiple report types, use this
	// now the only allowed type is "custom" and is managed client side
	private static final String REPORT_TYPE = "ReportType";

	private static final String[] KNOWN_PARAMETERS = {
		BUTTON_LABEL,
		REPORT_TYPE,
		REPORT_CODE,
		SAVE_TO_ALFRESCO,
		STORE_IN_PROCESS,
		FORCE_CSV,
		FORCE_PDF
	};

	public OpenReportWidgetFactory(final TemplateRepository templateRespository) {
		super(templateRespository);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(Map<String, Object> valueMap) {
		final String reportCode = readString(valueMap.get(REPORT_CODE));
		Validate.notEmpty(reportCode, REPORT_CODE + " is required");

		OpenReport widget = new OpenReport();
		widget.setReportCode(reportCode);
		widget.setPreset(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));
		forceFormat(valueMap, widget);

		return widget;
	}

	private void forceFormat(Map<String, Object> valueMap, OpenReport widget) {
		if (valueMap.containsKey(FORCE_PDF)) {
			widget.setForceFormat("pdf");
		} else if (valueMap.containsKey(FORCE_CSV)) {
			widget.setForceFormat("csv");
		}
	}
}