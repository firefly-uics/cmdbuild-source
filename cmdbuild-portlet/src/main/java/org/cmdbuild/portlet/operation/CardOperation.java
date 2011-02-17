package org.cmdbuild.portlet.operation;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Query;

public class CardOperation extends WSOperation {

	public CardOperation(final SOAPClient client) {
		super(client);
	}

	public int getCardId(final String classname, final Query query) {
		Log.PORTLET.debug("Get card with classname " + classname);
		final Attribute attribute = new Attribute();
		attribute.setName("Id");
		final List<Attribute> attributeList = new ArrayList<Attribute>();
		attributeList.add(attribute);
		int id = 0;
		final CardList result = getService().getCardList(classname, attributeList, query, null, 0, 0, null, null);
		id = result.getCards().get(0).getId();
		return id;
	}

	public CardList getCardList(final String classname, final List<Attribute> attributeList, final Query query,
			final List<Order> order, final int limit, final int offset, final String fullText, final CqlQuery cqlQuery) {
		Log.PORTLET.debug("Getting card list with following parameters:");
		Log.PORTLET.debug("- classname: " + classname);
		Log.PORTLET.debug("- limit: " + limit);
		Log.PORTLET.debug("- offset: " + offset);
		Log.PORTLET.debug("- text query: " + fullText);
		return getService().getCardList(classname, attributeList, query, order, limit, offset, fullText, cqlQuery);
	}

	public Card getCard(final String classname, final int cardid) {
		Log.PORTLET.debug("Getting card from " + classname + " with id " + cardid);
		return getService().getCard(classname, cardid, null);
	}

	public CardList getCardHistory(final String classname, final int cardid, final int limit, final int offset) {
		Log.PORTLET.debug("Getting history for card with classname " + classname + " and id " + cardid);
		return getService().getCardHistory(classname, cardid, limit, offset);
	}

	public int createCard(final Card card) {
		Log.PORTLET.debug("Creating card for class " + card.getClassName());
		return getService().createCard(card);
	}

	public boolean updateCard(final Card card) {
		Log.PORTLET.debug("Updating card for class " + card.getClassName());
		return getService().updateCard(card);
	}

	public Attribute getAttributeFromCard(final Card card, final String attributename) {
		for (final Attribute attribute : card.getAttributeList()) {
			if (attribute.getName().equals(attributename)) {
				return attribute;
			}
		}
		return null;
	}
}
