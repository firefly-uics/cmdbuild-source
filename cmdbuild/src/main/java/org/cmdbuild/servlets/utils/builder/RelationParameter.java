package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;

public class RelationParameter extends AbstractParameterBuilder<IRelation> {

	/*
	 * Note: since commit #1125 it does not create a new relation.
	 * We want to be sure we modify an existing relation.
	 */
	public IRelation build(HttpServletRequest r) throws AuthException, ORMException, NotFoundException {
		int class1Id = parameter(Integer.TYPE, "Class1Id", r);
		int card1Id = parameter(Integer.TYPE, "Card1Id", r);
		int class2Id = parameter(Integer.TYPE, "Class2Id", r);
		int card2Id = parameter(Integer.TYPE, "Card2Id", r);
		int domainId = parameter(Integer.TYPE, "DomainId", r);
		if (domainId == 0 || card1Id == 0 || class1Id == 0 || card2Id == 0 || class2Id == 0 ) {
			return null;
		} else {
			UserContext userCtx = new SessionVars().getCurrentUserContext();
			ICard card1 = userCtx.tables().get(class1Id).cards().get(card1Id);
			ICard card2 = userCtx.tables().get(class2Id).cards().get(card2Id);
			IDomain domain = userCtx.domains().get(domainId);
			return userCtx.relations().get(domain, card1, card2);
		}
	}
}
