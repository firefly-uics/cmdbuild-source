package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

public class ProcessTypeParameter extends AbstractParameterBuilder<ProcessType> {

	@Override
	public ProcessType build(final HttpServletRequest r) throws Exception {
		final int classId = parameter(Integer.TYPE, "idClass", r);
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		if (classId > 0)
			return UserOperations.from(userCtx).processTypes().get(classId);
		else
			return UserOperations.from(userCtx).processTypes().create();
	}
}
