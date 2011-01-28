package org.cmdbuild.portlet.operation;

import java.util.ArrayList;
import java.util.List;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.*;

public class CardOperation extends WSOperation {

    public CardOperation(SOAPClient client) {
        super(client);
    }

    public int getCardId(String classname, Query query) {
        Log.PORTLET.debug("Get card with classname " + classname);
        Attribute attribute = new Attribute();
        attribute.setName("Id");
        List<Attribute> attributeList = new ArrayList<Attribute>();
        attributeList.add(attribute);
        int id = 0;
        CardList result = getService().getCardList(classname, attributeList, query, null, 0, 0, null, null);
        id = result.getCards().get(0).getId();
        return id;
    }

    public CardList getCardList(String classname, List<Attribute> attributeList, Query query, List<Order> order, int limit, int offset, String fullText, CqlQuery cqlQuery) {
        Log.PORTLET.debug("Getting card list with following parameters:");
        Log.PORTLET.debug("- classname: " + classname);
        Log.PORTLET.debug("- limit: " + limit);
        Log.PORTLET.debug("- offset: " + offset);
        Log.PORTLET.debug("- text query: " + fullText);
        return getService().getCardList(classname, attributeList, query, order, limit, offset, fullText, cqlQuery);
    }

    public Card getCard(String classname, int cardid) {
        Log.PORTLET.debug("Getting card from " + classname + " with id " + cardid);
        return getService().getCard(classname, cardid, null);
    }

    public CardList getCardHistory(String classname, int cardid, int limit, int offset) {
        Log.PORTLET.debug("Getting history for card with classname " + classname + " and id " + cardid);
        return getService().getCardHistory(classname, cardid, limit, offset);
    }

    public int createCard(Card card) {
        Log.PORTLET.debug("Creating card for class " + card.getClassName());
        return getService().createCard(card);
    }

    public boolean updateCard(Card card) {
        Log.PORTLET.debug("Updating card for class " + card.getClassName());
        return getService().updateCard(card);
    }

    public Attribute getAttributeFromCard(Card card, String attributename) {
        for (Attribute attribute : card.getAttributeList()) {
            if (attribute.getName().equals(attributename)) {
                return attribute;
            }
        }
        return null;
    }
}
