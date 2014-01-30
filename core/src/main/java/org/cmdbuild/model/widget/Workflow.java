package org.cmdbuild.model.widget;

import java.util.Map;

public class Workflow extends Widget {

	private int workflowId;

	public int getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final int workflowId) {
		this.workflowId = workflowId;
	}

	private Map<String, Object> preset;

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public void setPreset(final Map<String, Object> preset) {
		this.preset = preset;
	}

	public Map<String, Object> getPreset() {
		return preset;
	}
}
