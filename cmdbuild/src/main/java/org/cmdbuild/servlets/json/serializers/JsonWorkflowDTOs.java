package org.cmdbuild.servlets.json.serializers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.servlets.json.serializers.JsonHistory.ValueAndDescription;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivity.CMActivityWidget;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.json.JSONException;

public class JsonWorkflowDTOs {

	private JsonWorkflowDTOs() {}

	public static class JsonActivityDefinition {

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

	/*
	 * The base info to show the activities in the grid
	 */
	public static class JsonActivityInstanceInfo {
		private final CMActivityInstance activityInstance;

		public JsonActivityInstanceInfo(final CMActivityInstance activityInstance) {
			this.activityInstance = activityInstance;
		}

		public String getId() {
			return activityInstance.getId();
		}

		public String getPerformerName() {
			return activityInstance.getPerformerName();
		}

		public String getDescription() throws CMWorkflowException {
			return activityInstance.getDefinition().getDescription();
		}

		public Boolean getWritePrivileges() {
			return true; //TODO: implement
		}
	}

	/*
	 * Merge the base info with the info in the activity definition
	 */
	public static class JsonActivityInstance extends JsonActivityDefinition {

		private final CMActivityInstance activityInstance;

		public JsonActivityInstance(CMActivityInstance activityInstance) throws CMWorkflowException {
			super(activityInstance.getDefinition());
			this.activityInstance = activityInstance;
		}

		public String getId() {
			return activityInstance.getId();
		}

		public String getPerformerName() {
			return activityInstance.getPerformerName();
		}

		public Boolean getWritePrivileges() {
			return true; //TODO: implement
		}
	}

	public static class JsonProcessCard extends AbstractJsonResponseSerializer {
		private CMProcessInstance processInstance;

		public JsonProcessCard(CMProcessInstance processInstance) {
			this.processInstance = processInstance;
		}

		public Long getId() {
			return processInstance.getCardId();
		}

		public String getBeginDate() {
			return formatDateTime(processInstance.getBeginDate());
		}

		public String getEndDate() {
			return formatDateTime(processInstance.getEndDate());
		}

		public Map<String,Object> getValues() {
			final Map<String, Object> output = new HashMap<String, Object>();
			for (CMAttribute attr : processInstance.getType().getAttributes()) {
				final String name = attr.getName();
				final Object value = javaToJsonValue(attr.getType(), processInstance.get(name));

				output.put(name, value);
			}

			return output;
		}

		public List<JsonActivityInstanceInfo> getActivityInstanceInfoList() throws CMWorkflowException {
			List<JsonActivityInstanceInfo> out = new ArrayList<JsonActivityInstanceInfo>();

			for (CMActivityInstance ai: processInstance.getActivities()) {
				out.add(new JsonActivityInstanceInfo(ai));
			}

			return out;
		}

		public String getFlowStatus() {
			return processInstance.getState().name();
		}

		public Object getClassId() {
			return processInstance.getType().getId();
		}

		public String getClassName() {
			return processInstance.getType().getName();
		}

		public String getClassDescription() {
			return processInstance.getType().getDescription();
		}

		protected Object javaToJsonValue(final CMAttributeType<?> type, final Object value) {
			return new JsonAttributeValueVisitor(type, value).valueForJson();
		}
	}
}