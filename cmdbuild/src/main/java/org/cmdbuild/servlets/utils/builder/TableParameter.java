package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

public class TableParameter extends AbstractParameterBuilder<ITable> {

	public static final String CLASS_ID_PARAMETER = "idClass";
	public static final String CLASS_NAME_PARAMETER = "className";

	@Override
	public ITable build(final HttpServletRequest r) throws Exception {
		final UserContext userCtx = new SessionVars().getCurrentUserContext();

		final int classId = parameter(Integer.TYPE, CLASS_ID_PARAMETER, r);
		if (classId > 0) {
			return UserOperations.from(userCtx).tables().get(classId);
		}

		final String className = parameter(String.class, CLASS_NAME_PARAMETER, r);
		if (className != null) {
			return UserOperations.from(userCtx).tables().get(className);
		}

		return UserOperations.from(userCtx).tables().create();
	}
}
