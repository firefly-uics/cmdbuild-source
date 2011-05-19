package org.cmdbuild.elements.interfaces;

public interface RelationFactory {

	IRelation create(IDomain domain, ICard card1, ICard card2);
	IRelation get(IDomain domain, ICard card1, ICard card2);
	RelationQuery list();
	RelationQuery list(ICard card);
}
