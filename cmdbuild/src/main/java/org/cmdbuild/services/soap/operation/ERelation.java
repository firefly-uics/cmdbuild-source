package org.cmdbuild.services.soap.operation;

import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.DirectedDomain.DomainDirection;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.types.Relation;

public class ERelation {

	private UserContext userCtx;

	public ERelation(UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public boolean deleteRelation(Relation relation) {

		Log.SOAP.debug("I'm going to delete relation between "
				+ relation.getCard1Id() + " (classname: "
				+ relation.getClass1Name() + ") and cardId "
				+ relation.getCard2Id() + "(classname: "
				+ relation.getClass2Name() + ")");
		ICard card1 = userCtx.tables().get(relation.getClass1Name()).cards()
				.get(relation.getCard1Id());
		ICard card2 = userCtx.tables().get(relation.getClass2Name()).cards()
				.get(relation.getCard2Id());
		IDomain idomain = userCtx.domains().get(relation.getDomainName());
		Log.SOAP.debug("Relation domain " + idomain.getDescription());
		IRelation irelation = userCtx.relations().get(idomain, card1, card2);

		irelation.delete();
		return true;

	}

	public boolean createRelation(Relation relation) {

		/*
		 * THIS IS A NO-NO!!! the "relation" object doesn't have, obvioulsy, an
		 * IRelation instance! btw. it's not supposed to EXIST yet!
		 */
		Log.SOAP.debug("I'm going to create relation between "
				+ relation.getClass1Name() + ":" + relation.getCard1Id()
				+ " and " + relation.getClass2Name() + ":"
				+ relation.getCard2Id());
		ICard card1 = userCtx.tables().get(relation.getClass1Name()).cards()
				.get(relation.getCard1Id());
		ICard card2 = userCtx.tables().get(relation.getClass2Name()).cards()
				.get(relation.getCard2Id());
		IDomain idomain = userCtx.domains().get(relation.getDomainName());
		IRelation irelation = userCtx.relations().create(idomain, card1, card2);
		Log.SOAP.debug("Relation domain " + idomain.getDescription());
		irelation.setCard1(card1);
		irelation.setCard2(card2);
		irelation.setSchema(idomain);

		irelation.save();

		return true;
	}

	public List<Relation> getRelationList(String domainName, String className, int cardId) {
		final IDomain domain = userCtx.domains().get(domainName);

		Iterable<IRelation> query;
		if (className != null && !className.isEmpty() && cardId > 0) {
			Log.SOAP.debug("Getting " + className + " relations for domain "
					+ domainName);
			final ICard card = userCtx.tables().get(className).cards().get(cardId);
			query = userCtx.relations().list(card).domain(domain);
		} else {
			Log.SOAP.debug("Getting all relation for domain " + domainName);
			query = userCtx.relations().list().domain(DirectedDomain.create(domain, DomainDirection.D));
		}

		final List<Relation> list = new LinkedList<Relation>();
		for (IRelation r : query) {
			list.add(new Relation(r));
		}
		return list;

	}

	public Relation[] getRelationHistory(Relation relation) {

		Log.SOAP.debug("Getting relation history between cardId "
				+ relation.getCard1Id() + " (classname: "
				+ relation.getClass1Name() + ") and cardId "
				+ relation.getCard2Id() + "(classname: "
				+ relation.getClass2Name() + ")");

		ICard card1 = userCtx.tables().get(relation.getClass1Name()).cards()
				.get(relation.getCard1Id());
		ICard card2 = userCtx.tables().get(relation.getClass2Name()).cards()
				.get(relation.getCard2Id());
		List<Relation> list = new LinkedList<Relation>();
		for (IRelation r : userCtx.relations().list(card1).history()) {
			if (card2 != null) {
				if (r.getCard2().equals(card2)) {
					list.add(new Relation(r));
				}
			} else {
				list.add(new Relation(r));
			}
		}
		Relation[] relationlist = new Relation[list.size()];
		relationlist = list.toArray(relationlist);
		return relationlist;
	}
}
