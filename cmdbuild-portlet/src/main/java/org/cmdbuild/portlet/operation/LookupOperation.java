package org.cmdbuild.portlet.operation;

import java.util.List;

import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Lookup;

public class LookupOperation extends WSOperation {

	public static final String FLOWSTATUS = "FlowStatus";

	public LookupOperation(final SOAPClient client) {
		super(client);
	}

	public List<Lookup> getLookupList(final String type) {
		Log.PORTLET.debug("Getting lookup with type " + type);
		return getService().getLookupList(type, null, true);
	}

	public Lookup getLookup(final int id) {
		Log.PORTLET.debug("Getting lookup with id " + id);
		return getService().getLookupById(id);
	}

	public List<Lookup> getFlowStatusLookup() {
		return getService().getLookupList(FLOWSTATUS, null, false);
	}
}
