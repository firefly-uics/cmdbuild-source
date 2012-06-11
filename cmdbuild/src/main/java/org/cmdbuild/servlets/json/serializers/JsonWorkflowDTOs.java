package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivity.CMActivityWidget;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

public interface JsonWorkflowDTOs {

	public class JsonActivityDefinition {

		final CMActivity inner;

		public JsonActivityDefinition(final CMActivity activity) {
			this.inner = activity;
		}

		public String getId() {
			return inner.getId();
		}

		public String getDescription() {
			return inner.getDescription();
		}

		public String getPerformerName() {
			return inner.getFirstRolePerformer().getName();
		}

		public String getInstructions() {
			return inner.getInstructions();
		}

		public Iterable<CMActivityVariableToProcess> getVariables() {
			return inner.getVariables();
		}

		public Iterable<CMActivityWidget> getWidgets() {
			return inner.getWidgets();
		}

		public static JsonActivityDefinition fromActivityDefinition(final CMActivity ad) {
			return new JsonActivityDefinition(ad);
		}
	}

}
