package org.cmdbuild.servlets.json.serializers;

import java.util.ArrayList;

import org.cmdbuild.elements.widget.Widget;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public interface JsonWorkflowDTOs {
	
	public class JsonActivityVariable {

		private final CMActivityVariableToProcess inner;

		public JsonActivityVariable(final CMActivityVariableToProcess variable) {
			this.inner = variable;
		}

		public String getName() {
			return inner.getName();
		}

		public CMActivityVariableToProcess.Type getType() {
			return inner.getType();
		}
	}

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

		public Iterable<JsonActivityVariable> getVariables() {
			return Iterables.transform(inner.getVariables(), new Function<CMActivityVariableToProcess, JsonActivityVariable>() {

				@Override
				public JsonActivityVariable apply(CMActivityVariableToProcess input) {
					return new JsonActivityVariable(input);
				}
				
			});
		}

		public Iterable<Widget> getWidgets() {
			return new ArrayList<Widget>(0);
		}

		public static JsonActivityDefinition fromActivityDefinition(final CMActivity ad) {
			return new JsonActivityDefinition(ad);
		}
	}

}
