package org.cmdbuild.elements;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.interfaces.RelationQuery;
import org.cmdbuild.elements.proxy.RelationProxy;
import org.cmdbuild.elements.proxy.RelationQueryProxy;
import org.cmdbuild.services.auth.UserContext;

public class RelationFactoryImpl implements RelationFactory {
	UserContext userCtx;

	public RelationFactoryImpl(UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public IRelation create(IDomain domain, ICard card1, ICard card2) {
		IRelation realRelation = new RelationImpl();
		realRelation.setSchema(domain);
		realRelation.setCard1(card1);
		realRelation.setCard2(card2);
		return new RelationProxy(realRelation, userCtx);
	}

	public IRelation get(IDomain domain, ICard card1, ICard card2) {
		IRelation realRelation = RelationImpl.get(domain, card1, card2);
		return new RelationProxy(realRelation, userCtx);
	}

	public RelationQuery list(ICard card) {
		RelationQuery relationQuery = new RelationQueryImpl(card);
		return new RelationQueryProxy(relationQuery, userCtx);
	}

	public RelationQuery list() {
		RelationQuery relationQuery = new RelationQueryImpl();
		return new RelationQueryProxy(relationQuery, userCtx);
	}
}
