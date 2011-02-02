package org.cmdbuild.portlet.operation;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.*;

public class ProcessOperation extends WSOperation {

    public ProcessOperation(SOAPClient client) {
        super(client);
    }

    public List<AttributeSchema> orderAttributeSchema(AttributeSchema[] attributeSchemaArray) {
        List<AttributeSchema> orderedList = new LinkedList<AttributeSchema>();
        if (attributeSchemaArray != null) {
            for (AttributeSchema attribute : attributeSchemaArray) {
                orderedList.add(attribute.getIndex(), attribute);
            }
        }
        return orderedList;
    }

    public ActivitySchema getStartActivityTemplate(String classname) {
        Log.PORTLET.debug("Getting attribute schema for process " + classname);
        return getService().getActivityObjects(classname, null);
    }

    public ActivitySchema getActivity(String classname, int id) {
        Log.PORTLET.debug("Getting attribute schema for process " + classname + " with id " + id);
        return getService().getActivityObjects(classname, id);
    }

    public int updateWorkflow(Card card, List<WorkflowWidgetSubmission> submissions) throws RemoteException {
        Log.PORTLET.debug("Updating process " + card.getClassName());
        return getService().updateWorkflow(card, true, submissions).getProcessid();
    }

    public String getProcessHelp(String classname) {
        Log.PORTLET.debug("Getting help for process " + classname);
        return getService().getProcessHelp(classname, null);
    }
}
