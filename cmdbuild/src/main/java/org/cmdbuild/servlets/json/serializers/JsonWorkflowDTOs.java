package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivity.CMActivityWidget;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

public interface JsonWorkflowDTOs {

	public class JsonActivityDefinition {

		private final CMActivity activity;

		public JsonActivityDefinition(final CMActivity activity) {
			this.activity = activity;
		}

		public String getPerformerName() {
			return activity.getFirstRolePerformer().getName();
		}

		public String getDescription() {
			return activity.getDescription();
		}

		public String getInstructions() {
			return activity.getInstructions();
		}

		public Iterable<CMActivityVariableToProcess> getVariables() {
			return activity.getVariables();
		}

		public Iterable<CMActivityWidget> getWidgets() {
			return activity.getWidgets();
		}

		public static JsonActivityDefinition fromActivityDefinition(final CMActivity ad) {
			return new JsonActivityDefinition(ad);
		}
	}

	public class JsonActivityInstance extends JsonActivityDefinition {

		private final CMActivityInstance activityInstance;

		public JsonActivityInstance(CMActivityInstance activityInstance) throws CMWorkflowException {
			super(activityInstance.getDefinition());
			this.activityInstance = activityInstance;
		}

		public String getId() {
			return activityInstance.getId();
		}

		public String getPerformerName() {
			throw new UnsupportedOperationException("Not implemented yet");
		}
	}
}
