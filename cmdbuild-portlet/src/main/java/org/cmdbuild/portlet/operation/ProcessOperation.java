package org.cmdbuild.portlet.operation;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.ActivitySchema;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;

public class ProcessOperation extends WSOperation {

	public ProcessOperation(final SOAPClient client) {
		super(client);
	}

	public List<AttributeSchema> orderAttributeSchema(final AttributeSchema[] attributeSchemaArray) {
		final List<AttributeSchema> orderedList = new LinkedList<AttributeSchema>();
		if (attributeSchemaArray != null) {
			for (final AttributeSchema attribute : attributeSchemaArray) {
				orderedList.add(attribute.getIndex(), attribute);
			}
		}
		return orderedList;
	}

	public ActivitySchema getStartActivityTemplate(final String classname) {
		Log.PORTLET.debug("Getting attribute schema for process " + classname);
		return getService().getActivityObjects(classname, null);
	}

	public ActivitySchema getActivity(final String classname, final int id) {
		Log.PORTLET.debug("Getting attribute schema for process " + classname + " with id " + id);
		return getService().getActivityObjects(classname, id);
	}

	public int updateWorkflow(final Card card, final List<WorkflowWidgetSubmission> submissions) throws RemoteException {
		Log.PORTLET.debug("Updating process " + card.getClassName());
		return getService().updateWorkflow(card, true, submissions).getProcessid();
	}

	public String getProcessHelp(final String classname) {
		Log.PORTLET.debug("Getting help for process " + classname);
		return getService().getProcessHelp(classname, null);
	}
}
