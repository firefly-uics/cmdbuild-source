package org.cmdbuild.portlet.operation;

import org.cmdbuild.portlet.plugin.CMPortletPluginLibrary;
import org.cmdbuild.portlet.plugin.CMPortletPlugin;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.exception.WebserviceException.WebserviceExceptionType;
import org.cmdbuild.portlet.layout.ComponentLayout;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.*;

public class ReferenceOperation extends WSOperation {

    public ReferenceOperation(SOAPClient client) {
        super(client);
    }

    public List<Reference> getReferenceList(ComponentLayout layout, int limit, int offset) {
        return getReferenceList(layout, null, null, limit, offset);
    }

    public List<Reference> getReferenceList(ComponentLayout layout, Query query, List<Order> order, int limit, int offset) {
        for (CMPortletPlugin plugin : CMPortletPluginLibrary.getPlugins()) {
            List<Reference> r = plugin.getReferenceList(layout, query, order, limit, offset);
            if (r != null) {
                return r;
            }
        }
        return getReferenceListDefault(layout, query, order, limit, offset);
    }

    private List<Reference> getReferenceListDefault(ComponentLayout layout, Query query, List<Order> order, int limit, int offset) {
        Log.PORTLET.debug("Getting reference for class " + layout.getSchema().getReferencedClassName());
        if (order == null) {
            order = defaultOrder();
        }
        return getService().getReference(layout.getSchema().getReferencedClassName(), query, null, limit, offset, null);
    }
    
    private List<Order> defaultOrder() {
        Order orderElement = new Order();
        orderElement.setColumnName("Description");
        orderElement.setType("ASC");
        List<Order> order = new ArrayList<Order>();
        order.add(orderElement);
        return order;
    }
}
