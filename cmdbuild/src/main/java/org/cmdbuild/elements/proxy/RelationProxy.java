package org.cmdbuild.elements.proxy;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.services.auth.UserContext;

public class RelationProxy extends RelationForwarder {
	protected UserContext userCtx;

	public RelationProxy(IRelation relation, UserContext userCtx) {
		super(relation);
		this.userCtx = userCtx;
	}

	@Override
	public ICard getCard1() {
		return proxyCardIfNeeded(super.getCard1());
	}

	@Override
	public ICard getCard2() {
		return proxyCardIfNeeded(super.getCard2());
	}

	private ICard proxyCardIfNeeded(ICard card) {
		// card can be null on counted relations when count is over the limit
		if ((card != null) && !(card instanceof CardProxy))
			return new CardProxy(card, userCtx);
		return card;
	}

	@Override
	public IDomain getSchema() {
		IDomain domain =  super.getSchema();
		if (!(domain instanceof DomainProxy))
			domain = new DomainProxy(domain, userCtx);
		return domain;
	}

	@Override
	public void delete() {
		userCtx.privileges().assureWritePrivilege(r.getSchema());
		super.delete();
	}

	@Override
	public void save() {
		userCtx.privileges().assureCreatePrivilege(r.getSchema());
		r.setValue("User", userCtx.getUsername());
		super.save();
	}
}
