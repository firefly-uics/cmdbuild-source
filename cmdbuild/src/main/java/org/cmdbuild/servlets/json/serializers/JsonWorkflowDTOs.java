package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.workflow.CMActivity;

public interface JsonWorkflowDTOs {

	public class JsonActivityDefinition {

		final CMActivity inner;

		public JsonActivityDefinition(final CMActivity activity) {
			this.inner = activity;
		}

		public String getActivityDescription() {
			return inner.getName();
		}

		public String getPerformerDescription() {
			// TODO Description, not name!
			return inner.getFirstRolePerformer().getName();
		}

		public String getInstructions() {
			return null;
		}

		public static JsonActivityDefinition fromActivityDefinition(final CMActivity ad) {
			return new JsonActivityDefinition(ad);
		}
	}

}
