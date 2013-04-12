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
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.soap.types.Relation;

public class ERelation {

	private final UserContext userCtx;

	public ERelation(final UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public boolean deleteRelation(final Relation relation) {

		Log.SOAP.debug("I'm going to delete relation between " + relation.getCard1Id() + " (classname: "
				+ relation.getClass1Name() + ") and cardId " + relation.getCard2Id() + "(classname: "
				+ relation.getClass2Name() + ")");
		final ICard card1 = UserOperations.from(userCtx).tables().get(relation.getClass1Name()).cards()
				.get(relation.getCard1Id());
		final ICard card2 = UserOperations.from(userCtx).tables().get(relation.getClass2Name()).cards()
				.get(relation.getCard2Id());
		final IDomain idomain = UserOperations.from(userCtx).domains().get(relation.getDomainName());
		Log.SOAP.debug("Relation domain " + idomain.getDescription());
		final IRelation irelation = UserOperations.from(userCtx).relations().get(idomain, card1, card2);

		irelation.delete();
		return true;

	}

	public List<Relation> getRelationList(final String domainName, final String className, final int cardId) {
		final IDomain domain = UserOperations.from(userCtx).domains().get(domainName);

		Iterable<IRelation> query;
		if (className != null && !className.isEmpty() && cardId > 0) {
			Log.SOAP.debug("Getting " + className + " relations for domain " + domainName);
			final ICard card = UserOperations.from(userCtx).tables().get(className).cards().get(cardId);
			query = UserOperations.from(userCtx).relations().list(card).domain(domain);
		} else {
			Log.SOAP.debug("Getting all relation for domain " + domainName);
			query = UserOperations.from(userCtx).relations().list()
					.domain(DirectedDomain.create(domain, DomainDirection.D));
		}

		final List<Relation> list = new LinkedList<Relation>();
		for (final IRelation r : query) {
			list.add(new Relation(r));
		}
		return list;

	}

	public Relation[] getRelationHistory(final Relation relation) {
		Log.SOAP.debug(String.format("Getting relation history for card (%d, %s)", relation.getCard1Id(),
				relation.getClass1Name()));
		if (relation.getCard2Id() > 0 || relation.getClass2Name() != null || relation.getDomainName() != null) {
			throw new UnsupportedOperationException("You fool!");
		}
		final ICard card1 = UserOperations.from(userCtx).tables().get(relation.getClass1Name()).cards()
				.get(relation.getCard1Id());
		final List<Relation> list = new LinkedList<Relation>();
		for (final IRelation r : UserOperations.from(userCtx).relations().list(card1).history()) {
			list.add(new Relation(r));
		}
		final Relation[] relationlist = list.toArray(new Relation[list.size()]);
		return relationlist;
	}

}
