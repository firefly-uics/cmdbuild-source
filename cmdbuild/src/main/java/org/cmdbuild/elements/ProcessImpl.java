package org.cmdbuild.elements;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.proxy.CardProxy;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.elements.interfaces.Process;

public class ProcessImpl extends CardProxy implements Process {

	public ProcessImpl(ICard card, UserContext userCtx) {
		super(card, userCtx);
	}

}
