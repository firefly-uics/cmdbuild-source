package org.cmdbuild.portlet.operation;

import java.util.List;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.*;

public class LookupOperation extends WSOperation{

    public static final String FLOWSTATUS = "FlowStatus";

    public LookupOperation(SOAPClient client) {
        super(client);
    }

    public List<Lookup> getLookupList(String type) {
        Log.PORTLET.debug("Getting lookup with type " + type);
        return getService().getLookupList(type, null, true);
    }

    public Lookup getLookup(int id) {
        Log.PORTLET.debug("Getting lookup with id " + id);
        return getService().getLookupById(id);
    }

    public List<Lookup> getFlowStatusLookup() {
        return getService().getLookupList(FLOWSTATUS, null, false);
    }
}
