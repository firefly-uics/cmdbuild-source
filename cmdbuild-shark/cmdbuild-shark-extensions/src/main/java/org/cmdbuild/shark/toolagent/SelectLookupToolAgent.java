package org.cmdbuild.shark.toolagent;

import org.cmdbuild.services.soap.Lookup;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.type.LookupType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

/**
 * Select a Lookup based on the passed parameters. A LookupType instance will be
 * returned.
 * 
 */
public class SelectLookupToolAgent extends AbstractCmdbuildWSToolAgent {

	private enum ApplicationName {
		selectLookup, selectLookupById, selectLookupByTypeDesc, selectLookupByTypeCode;
	}

	@Override
	protected void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception {
		Object out = null;

		final ApplicationName ap = ApplicationName.valueOf(toolInfoID);

		switch (ap) {
		case selectLookup:
			final LookupType lookup = selectLookupById(stub, (Long) get(params, "LookupId"));
			out = lookup.getDescription();
			break;
		case selectLookupById:
			out = selectLookupById(stub, (Long) get(params, "LookupId"));
			break;
		case selectLookupByTypeDesc:
			out = selectLookupByTypeDescription(stub, (String) get(params, "Type"),
					(String) get(params, "Description"));
			break;
		case selectLookupByTypeCode:
			out = selectLookupByTypeCode(stub, (String) get(params, "Type"), (String) get(
					params, "Code"));
			break;

		}
		setOutputValue(params, out);
	}

	private void setOutputValue(final AppParameter[] params, final Object out) {
		for (final AppParameter p : params) {
			if (!p.the_mode.equals("IN"))
				p.the_value = out;
		}
	}

	private LookupType selectLookupByTypeDescription(final Private stub, final String type, final String value)
			throws Exception {
		return convert(stub.getLookupList(type, value, false).get(0));
	}

	private LookupType selectLookupByTypeCode(final Private stub, final String type, final String value)
			throws Exception {
		return convert(stub.getLookupListByCode(type, value, false).get(0));
	}

	private LookupType selectLookupById(final Private stub, final long id) throws Exception {
		return convert(stub.getLookupById((int) id));
	}

	private LookupType convert(final Lookup lookup) {
		return new LookupType(lookup.getId(), lookup.getType(), lookup.getDescription(), lookup.getCode());
	}
}
