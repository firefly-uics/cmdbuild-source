package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;

public class CardParameter extends AbstractParameterBuilder<ICard> {

	public static final String CLASS_ID = "IdClass";
	public static final String ID = "Id";
	public static final String CLASS_NAME = "ClassName";
	
	public ICard build(HttpServletRequest r) throws AuthException, ORMException, NotFoundException {
		final int cardId = parameter(Integer.TYPE, ID, r);
		final String className = parameter(String.class, CLASS_NAME, r);

		if (className != null) {
			return buildCard(className, cardId);
		} else {
			final int classId = parameter(Integer.TYPE, CLASS_ID, r);
			return buildCard(classId, cardId);
		}
	}

	private ICard buildCard(final int classId, final int cardId) {
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		return buildCard(userCtx.tables().get(classId), cardId);
	}

	private ICard buildCard(final String className, final int cardId) {
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		return buildCard(userCtx.tables().get(className), cardId);
	}

	private ICard buildCard(final ITable table, final int cardId) {
		Log.JSONRPC.debug("build card className:"+table.getName()+", id:"+cardId);
		if(cardId > 0){
			return table.cards().get(cardId);
		} else {
			return table.cards().create();
		}
	}
}
