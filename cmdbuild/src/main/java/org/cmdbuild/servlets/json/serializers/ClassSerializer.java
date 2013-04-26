package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Constants;
import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.json.JSONException;
import org.json.JSONObject;

public class ClassSerializer extends Serializer {

	private static final String WRITE_PRIVILEGE = "priv_write", CREATE_PRIVILEGE = "priv_create";

	public static ClassSerializer newInstance() {
		return new ClassSerializer();
	}

	private ClassSerializer() {
		// prevents instantiation
	}

	/**
	 * keep until the end of integration just to be sure that actual
	 * serialization has all what we need
	 */
	@OldDao
	private JSONObject toClient(final UserProcessClass element, final String wrapperLabel,
			final boolean addManagementInfo) throws JSONException, CMWorkflowException {
		final JSONObject jsonObject = toClient(CMClass.class.cast(element), wrapperLabel);

		jsonObject.put("type", "processclass");
		if (addManagementInfo) {
			jsonObject.put("startable", element.isStartable());
			jsonObject.put("userstoppable", element.isStoppable());
		}

		return jsonObject;
	}

	/**
	 * keep until the end of integration just to be sure that actual
	 * serialization has all what we need
	 */
	@OldDao
	public JSONObject toClient(final CMClass cmClass, final String wrapperLabel) throws JSONException {
		final JSONObject jsonObject = new JSONObject();

		jsonObject.put("type", "class");
		jsonObject.put("id", cmClass.getId());
		jsonObject.put("name", cmClass.getName());
		jsonObject.put("text", cmClass.getDescription());
		jsonObject.put("superclass", cmClass.isSuperclass());
		jsonObject.put("active", cmClass.isActive());
		jsonObject.put("tableType", cmClass.holdsHistory() ? "standard" : "simpletable");
		jsonObject.put("selectable", !cmClass.getName().equals(Constants.BASE_CLASS_NAME));

		// TODO complete
		// addGeoFeatureTypes(jsonTable, table);
		addMetadata(jsonObject, cmClass);
		addAccessPrivileges(cmClass, jsonObject);

		final CMClass parent = cmClass.getParent();
		if (parent != null) {
			jsonObject.put("parent", parent.getId());
		}

		// Wrap the serialization if required
		if (wrapperLabel != null) {
			final JSONObject out = new JSONObject();
			out.put(wrapperLabel, jsonObject);
			return out;
		} else {
			return jsonObject;
		}
	}

	public JSONObject toClient(final UserProcessClass element, final boolean addManagementInfo) throws JSONException,
			CMWorkflowException {
		return toClient(element, null, addManagementInfo);
	}

	public JSONObject toClient(final CMClass element) throws JSONException {
		return toClient(element, null);
	}

	private static void addAccessPrivileges(final CMEntryType entryType, final JSONObject json) throws JSONException {
		final OperationUser user = new SessionVars().getUser();
		final boolean writePrivilege = user.hasWriteAccess(entryType);
		json.put(WRITE_PRIVILEGE, writePrivilege);
		boolean createPrivilege = writePrivilege;
		if (entryType instanceof CMClass) {
			createPrivilege &= !((CMClass) entryType).isSuperclass();
		}

		json.put(CREATE_PRIVILEGE, createPrivilege);
	}

}
