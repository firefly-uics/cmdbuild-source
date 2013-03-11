package org.cmdbuild.servlets.json.serializers;

import java.util.List;

import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.privileges.SecurityLogic.PrivilegeInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.cmdbuild.servlets.json.ComunicationConstants.*;

public class PrivilegeSerializer {

	public static JSONObject serializePrivilege(final PrivilegeInfo privilege) throws JSONException {
		final JSONObject jsonPrivilege = new JSONObject();
		jsonPrivilege.put(GROUP_ID, privilege.getGroupId());
		jsonPrivilege.put(PRIVILEGE_WRITE, privilege.mode.equals(PrivilegeMode.WRITE.getValue()));
		jsonPrivilege.put(PRIVILEGE_READ, privilege.mode.equals(PrivilegeMode.READ.getValue()));
		jsonPrivilege.put(PRIVILEGE_NONE, privilege.mode.equals(PrivilegeMode.NONE.getValue()));
		jsonPrivilege.put(PRIVILEGE_OBJ_ID, privilege.getPrivilegedObjectId());
		jsonPrivilege.put(PRIVILEGE_OBJ_NAME, privilege.getPrivilegedObjectName());
		jsonPrivilege.put(PRIVILEGE_OBJ_DESCRIPTION, privilege.getPrivilegedObjectDescription());

		return jsonPrivilege;
	}

	public static JSONObject serializePrivilegeList(final List<PrivilegeInfo> privileges) throws JSONException {
		final JSONArray privilegeList = new JSONArray();
		final JSONObject out = new JSONObject();
		for (final PrivilegeInfo privilege : privileges) {
			try {
				privilegeList.put(serializePrivilege(privilege));
			} catch (final NotFoundException e) {
				Log.PERSISTENCE.warn( //
						"Class OID not found (" + //
						privilege.getPrivilegedObjectId() + //
						") while searching for grant for group " + //
						privilege.getGroupId() //
					);
			}
		}

		out.put(PRIVILEGES, privilegeList);
		return out;
	}

}
