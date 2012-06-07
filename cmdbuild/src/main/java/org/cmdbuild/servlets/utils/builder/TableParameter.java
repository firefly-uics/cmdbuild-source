package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;

public class TableParameter extends AbstractParameterBuilder<ITable> {

	public static final String CLASS_ID_PARAMETER = "idClass";
	public static final String CLASS_NAME_PARAMETER = "className";

	public ITable build(HttpServletRequest r) throws Exception {
		UserContext userCtx = new SessionVars().getCurrentUserContext();

		int classId = parameter(Integer.TYPE,CLASS_ID_PARAMETER,r);
		if (classId > 0) {
			return userCtx.tables().get(classId);
		}

		String className = parameter(String.class,CLASS_NAME_PARAMETER,r);
		if (className != null) {
			return userCtx.tables().get(className);
		}

		return userCtx.tables().create();
	}
}
