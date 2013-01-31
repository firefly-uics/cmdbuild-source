package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.json.JSONException;
import org.json.JSONObject;

public class ClassSerializer extends Serializer{
	public static JSONObject toClient(final CMClass cmClass, final String wrapperLabel) throws JSONException {
		final JSONObject jsonTable = new JSONObject();

		jsonTable.put("type", getClassType(cmClass.getName()));
		// TODO complete
		// if (table.isActivity()) {
		// jsonTable.put("userstoppable", table.isUserStoppable());
		// } else {
		// jsonTable.put("type", "class");
		// }

		jsonTable.put("id", cmClass.getId());
		jsonTable.put("name", cmClass.getName());
		jsonTable.put("text", cmClass.getDescription());
		jsonTable.put("superclass", cmClass.isSuperclass());
		jsonTable.put("active", cmClass.isActive());

		jsonTable.put("tableType", cmClass.holdsHistory() ? "standard" : "simpletable");
		jsonTable.put("selectable", !cmClass.getName().equals(Constants.BASE_CLASS_NAME));

		// TODO complete
		// addMetadataAndAccessPrivileges(jsonTable, table);
		// addGeoFeatureTypes(jsonTable, table);

		final CMClass parent = cmClass.getParent();
		if (parent != null) {
			jsonTable.put("parent", parent.getId());
		}

		// Wrap the serialization if required
		if (wrapperLabel != null) {
			JSONObject out = new JSONObject();
			out.put(wrapperLabel, jsonTable);
			return out;
		} else {
			return jsonTable;
		}
	}

	public static JSONObject toClient(final CMClass cmClass) throws JSONException {
		return toClient(cmClass, null);
	}


	/**
	 * @deprecated use serialize(CMClass) instead.
	 */
	@Deprecated
	public static JSONObject toClient(final ITable table) throws JSONException {
		final JSONObject jsonTable = new JSONObject();

		if (table.isActivity()) {
			jsonTable.put("type", "processclass");
			jsonTable.put("userstoppable", table.isUserStoppable());
		} else {
			jsonTable.put("type", "class");
		}

		jsonTable.put("id", table.getId());
		jsonTable.put("name", table.getName());
		jsonTable.put("text", table.getDescription());
		jsonTable.put("superclass", table.isSuperClass());
		jsonTable.put("active", table.getStatus().isActive());

		if (table.getTableType() == CMTableType.SIMPLECLASS) {
			jsonTable.put("tableType", "simpletable");
		} else {
			jsonTable.put("tableType", "standard");
		}

		if (table.isTheTableClass()) {
			jsonTable.put("selectable", false);
		} else {
			jsonTable.put("selectable", true);
		}

		addMetadataAndAccessPrivileges(jsonTable, table);
		addParent(table, jsonTable);
		return jsonTable;
	}

	@Deprecated
	public static JSONObject toClient(final ITable table, final UserProcessClass pc) throws JSONException {
		final JSONObject jsonProcess = toClient(table);
		boolean isStartable = !pc.isSuperclass();
		if (isStartable) {
			try {
				isStartable = pc.isStartable();
			} catch (final CMWorkflowException e) {
				isStartable = false;
			}
		}

		// add this to look in the XPDL if the current user has
		// the privileges to start the process and ignore the table privileges
		// (priv_create)
		jsonProcess.put("startable", isStartable);
		return jsonProcess;
	}

	/**
	 * @deprecated use addParent(CMClass, JSONObject) instead.
	 */
	@Deprecated
	private static void addParent(final ITable table, final JSONObject jsonTable) throws JSONException {
		try {
			if (table.getTableType() != CMTableType.SIMPLECLASS && !table.isTheTableActivity()) {
				jsonTable.put("parent", table.getParent().getId());
			}
		} catch (final NullPointerException e) {
			// If the table has no parent
		}
	}
}
