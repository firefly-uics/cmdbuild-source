package org.cmdbuild.portlet.operation;

import java.util.List;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.*;


public class RelationOperation extends WSOperation {

    public RelationOperation(SOAPClient client) {
        super(client);
    }

    public List<Relation> getRelationList(String domain, String classname, int cardid) {
        Log.PORTLET.debug("Getting relations with following parameters");
        Log.PORTLET.debug("Domain: " + domain);
        Log.PORTLET.debug("Classname: " + classname);
        Log.PORTLET.debug("Card ID: " + cardid);
        return getService().getRelationList(domain, classname, cardid);
    }
}
