package org.cmdbuild.elements;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.interfaces.RelationQuery;
import org.cmdbuild.elements.proxy.RelationProxy;
import org.cmdbuild.elements.proxy.RelationQueryProxy;
import org.cmdbuild.elements.proxy.RelationQueryProxy.ProxedIterableRelation;
import org.cmdbuild.services.auth.UserContext;
import org.springframework.beans.factory.annotation.Autowired;

public class RelationFactoryImpl implements RelationFactory {
	private final UserContext userCtx;

	@Autowired
	private CMBackend backend = CMBackend.INSTANCE;

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

	public IRelation create(IDomain domain) {
		IRelation realRelation = new RelationImpl();
		realRelation.setSchema(domain);
		return new RelationProxy(realRelation, userCtx);
	}

	public IRelation get(IDomain domain, ICard card1, ICard card2) {
		IRelation realRelation = backend.getRelation(domain, card1, card2);
		return new RelationProxy(realRelation, userCtx);
	}

	public IRelation get(IDomain domain, int id) {
		IRelation realRelation = backend.getRelation(domain, id);
		return new RelationProxy(realRelation, userCtx);
	}

	public Iterable<IRelation> list(DirectedDomain directedDomain, ICard sourceCard) {
		Iterable<IRelation> realIterableRelation = backend.getRelationList(directedDomain, sourceCard.getId());
		return new ProxedIterableRelation(realIterableRelation, userCtx);
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
