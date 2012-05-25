package org.cmdbuild.servlets.json.serializers;

import java.util.ArrayList;

import org.cmdbuild.elements.widget.Widget;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

public interface JsonWorkflowDTOs {

	public class JsonActivityDefinition {

		final CMActivity inner;

		public JsonActivityDefinition(final CMActivity activity) {
			this.inner = activity;
		}

		public String getName() {
			return inner.getName();
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

		public Iterable<Widget> getWidgets() {
			return new ArrayList<Widget>(0);
		}

		public static JsonActivityDefinition fromActivityDefinition(final CMActivity ad) {
			return new JsonActivityDefinition(ad);
		}
	}

}
