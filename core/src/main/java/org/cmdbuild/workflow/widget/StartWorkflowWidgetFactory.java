package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.model.widget.Workflow;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class StartWorkflowWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "startWorkflow";

	public static final String WORKFLOW_CODE = "WorkflowCode";

	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, WORKFLOW_CODE };

	public StartWorkflowWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final String workflowCode = readString(valueMap.get(WORKFLOW_CODE));
		Validate.notEmpty(workflowCode, WORKFLOW_CODE + " is required");

		final Workflow widget = new Workflow();
		widget.setWorkflowName(workflowCode);
		widget.setPreset(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));

		return widget;
	}

}
