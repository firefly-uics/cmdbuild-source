package org.cmdbuild.elements;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.CardFactory;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.CardQueryProxy;
import org.cmdbuild.elements.proxy.CardProxy;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.services.auth.UserContext;

public class CardFactoryImpl implements CardFactory {
	UserContext userCtx;
	ITable table;

	public CardFactoryImpl(ITable table, UserContext userCtx) {
		this.table = table;
		this.userCtx = userCtx;
	}

	public ICard create() {
		// create privileges check is on save to allow template cards creation
		userCtx.privileges().assureReadPrivilege(table);
		ICard realCard = new CardImpl(table);
		return new CardProxy(realCard, userCtx);
	}

	public ICard get(int cardId) throws NotFoundException {
		ICard realCard = new LazyCard(table, cardId);
		return new CardProxy(realCard, userCtx);
	}

	public CardQuery list() {
		CardQuery cardQuery = new CardQueryImpl(table);
		return new CardQueryProxy(cardQuery, userCtx);
	}
}
