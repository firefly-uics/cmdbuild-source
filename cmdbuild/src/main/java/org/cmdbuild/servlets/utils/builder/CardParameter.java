package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;

public class CardParameter extends AbstractParameterBuilder<ICard> {

	public ICard build(HttpServletRequest r) throws AuthException, ORMException, NotFoundException {
		int classId = parameter(Integer.TYPE, "IdClass", r);
		int cardId = parameter(Integer.TYPE, "Id", r);
		Log.JSONRPC.debug("build card classId:"+classId+", id:"+cardId);
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		if(cardId > 0){
			return userCtx.tables().get(classId).cards().get(cardId);
		} else {
			return userCtx.tables().get(classId).cards().create();
		}
	}
}
