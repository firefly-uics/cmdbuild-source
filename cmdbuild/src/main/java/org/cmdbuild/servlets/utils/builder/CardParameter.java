package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

public class CardParameter extends AbstractParameterBuilder<ICard> {

	@Override
	public ICard build(final HttpServletRequest r) throws AuthException, ORMException, NotFoundException {
		final int classId = parameter(Integer.TYPE, "IdClass", r);
		final int cardId = parameter(Integer.TYPE, "Id", r);
		Log.JSONRPC.debug("build card classId:" + classId + ", id:" + cardId);
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		if (cardId > 0) {
			return UserOperations.from(userCtx).tables().get(classId).cards().get(cardId);
		} else {
			return UserOperations.from(userCtx).tables().get(classId).cards().create();
		}
	}
}
