package org.cmdbuild.elements;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.Process;
import org.cmdbuild.elements.interfaces.ProcessFactory;
import org.cmdbuild.elements.interfaces.ProcessQuery;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.services.auth.UserContext;

public class ProcessFactoryImpl implements ProcessFactory {

	UserContext userCtx;
	ProcessType processType;

	public ProcessFactoryImpl(ProcessType processType, UserContext userCtx) {
		this.processType = processType;
		this.userCtx = userCtx;
	}

	public Process create() {
		userCtx.privileges().assureCreatePrivilege(processType);
		ICard realCard = new CardImpl(processType);
		return new ProcessImpl(realCard, userCtx);
	}

	public Process get(int cardId) throws NotFoundException {
		ICard realCard = new LazyCard(processType, cardId);
		return new ProcessImpl(realCard, userCtx);
	}

	public ProcessQuery list() {
		CardQuery cardQuery = new CardQueryImpl(processType);
		return new ProcessQueryImpl(cardQuery, userCtx);
	}

}
