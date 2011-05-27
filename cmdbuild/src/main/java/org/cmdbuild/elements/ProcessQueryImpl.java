package org.cmdbuild.elements;

import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ProcessQuery;
import org.cmdbuild.elements.proxy.CardQueryProxy;
import org.cmdbuild.services.auth.UserContext;

public class ProcessQueryImpl extends CardQueryProxy implements ProcessQuery {

	public ProcessQueryImpl(CardQuery cardQuery, UserContext userCtx) {
		super(cardQuery, userCtx);
	}
}
