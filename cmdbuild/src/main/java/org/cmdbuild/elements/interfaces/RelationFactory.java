package org.cmdbuild.elements.interfaces;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.DirectedDomain;

@OldDao
@Deprecated
public interface RelationFactory {

	IRelation create(IDomain domain, ICard card1, ICard card2);
	IRelation create(IDomain domain);
	IRelation get(IDomain domain, ICard card1, ICard card2);
	IRelation get(IDomain domain, int id);
	Iterable<IRelation> list(DirectedDomain directedDomain, ICard sourceCard);
	RelationQuery list();
	RelationQuery list(ICard card);
}
