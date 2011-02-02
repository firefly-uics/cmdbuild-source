package org.cmdbuild.portlet.operation;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.metadata.User;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.*;

public class WSOperation {

    private Private service;

    public WSOperation(SOAPClient client) {
        service = client.getService();
    }

    public Private getService() {
        return service;
    }

    public User getUser(String email) {
        String usertable = PortletConfiguration.getInstance().getCMDBuildUserClass();
        if (!"".equals(usertable)) {
            return getUserInformations(usertable, email);
        } else {
            Log.PORTLET.debug("No user class defined in properties");
            return null;
        }
    }

    private User getUserInformations(String usertable, String email) {
        Log.PORTLET.debug("Getting information for user " + email);
        String[] mail = new String[1];
        mail[0] = email;
        Query query = new Query();
        Filter namefilter = new Filter();
        namefilter.setName(PortletConfiguration.getInstance().getCMDBuildUserEmail());
        namefilter.setOperator("EQUALS");
        namefilter.getValue().add(email);
        query.setFilter(namefilter);
        User user = null;
        CardList card = service.getCardList(usertable, null, query, null, 0, 0, null, null);
        if (card != null && card.getCards().size() == 1) {
        	int id = card.getCards().get(0).getId();
            user = new User();
            user.setEmail(email);
            user.setId(id);
            user.setName(getDescription(card.getCards().get(0).getAttributeList()));
        } else {
            if (card == null || card.getCards() == null) {
            	Log.PORTLET.error("Error querying for user in table " + usertable);
            } else if (card.getCards().isEmpty()) {
            	Log.PORTLET.error("No user in table " + usertable + " with email " + email);
            } else {
                Log.PORTLET.error("Too many users in table " + usertable + " with email " + email);
            }
        }
        return user;
    }

    private String getDescription(List<Attribute> attributes) {
        String description = "";
        for (Attribute attribute : attributes) {
            if ("Description".equals(attribute.getName())) {
                description = attribute.getValue();
            }
        }
        return description;
    }

    public MenuSchema getProcessMenu() {
        return service.getActivityMenuSchema();
    }

    public MenuSchema getClassMenu() {
        return service.getCardMenuSchema();
    }

    public MenuSchema getMenu() {
        return service.getMenuSchema();
    }

    public String getGroup(MenuSchema schema) {
        if (schema.getId() > 0) {
            for (Metadata m : schema.getMetadata()) {
                if (m != null && "runtime.groupname".equals(m.getKey())) {
                    return m.getValue();
                }
            }
            return "";
        } else {
            return getGroup(schema.getChildren().get(0));
        }
    }

    public List<AttributeSchema> getAttributeList(String classname) {
        Log.PORTLET.debug("Getting attribute schema for class " + classname);
        List<AttributeSchema> list = service.getAttributeList(classname);
        return sortAttributes(list);
    }

    private static List<AttributeSchema> sortAttributes(List<AttributeSchema> attributeCollection) {
        List<AttributeSchema> sortedAttributes = new LinkedList<AttributeSchema>();
        sortedAttributes.addAll(attributeCollection);
        Collections.sort(sortedAttributes, new Comparator<AttributeSchema>() {

            public int compare(AttributeSchema a1, AttributeSchema a2) {
                return (a1.getIndex() - a2.getIndex());
            }
        });

        return sortedAttributes;
    }
}
