package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;

public class ProcessTypeParameter extends AbstractParameterBuilder<ProcessType> {

	public ProcessType build(HttpServletRequest r) throws Exception {
		int classId = parameter(Integer.TYPE,"idClass",r);
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		if (classId > 0)
			return userCtx.processTypes().get(classId);
		else
			return userCtx.processTypes().create();
	}
}
