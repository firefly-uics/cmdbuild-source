package org.cmdbuild.shark.toolagent;

import java.util.LinkedList;

import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;
import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.shark.util.CmdbuildUtils.CmdbuildTableStruct;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.xpdl.elements.ExtendedAttributes;

public class ProcessStartToolAgent extends AbstractCmdbuildWSToolAgent {

	@Override
	protected void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception {
		final ExtendedAttributes eas = this.readParamsFromExtAttributes((String) parameters[0].the_value);
		final String className = eas.getFirstExtendedAttributeForName("ProcessClass").getVValue();
		final CmdbuildTableStruct tableStruct = CmdbuildUtils.getInstance().getStructureFromName(className);
		boolean save = false;
		if (eas.containsElement("Save")) {
			save = eas.getFirstExtendedAttributeForName("Save").getVValue().equals("1");
		} else if (eas.containsElement("Complete")) {
			save = eas.getFirstExtendedAttributeForName("Complete").getVValue().equals("1");
		}

		final Card card = new Card();
		card.setClassName(className);

		for (int i = 1; i < params.length; i++) {
			final AppParameter param = params[i];
			final String attrName = params[i].the_formal_name;
			final Object attrValue = params[i].the_value;
			if (!param.the_mode.equals("IN")) {
				continue;
			}
			final Object val = param.the_value;
			if (val == null) { // skip null values
				continue;
			}
			final String serialized = AbstractWSToolAgent.sharkAttributeToWSString(stub, tableStruct, card, attrName,
					attrValue);
			if (serialized != null) {
				final Attribute attr = new Attribute();
				attr.setName(attrName);
				attr.setValue(serialized);
				card.getAttributeList().add(attr);
			}
		}

		final String procInstId = stub.updateWorkflow(card, save, new LinkedList<WorkflowWidgetSubmission>())
				.getProcessinstanceid();
		int cmdbCardId = -1;

		for (int i = 1; i < params.length; i++) {
			final AppParameter param = params[i];
			if (param.the_mode.equals("IN")) {
				continue;
			}
			if (param.the_class.equals(String.class)) {
				param.the_value = procInstId;
			} else if (param.the_class.equals(Long.class)) {
				if (cmdbCardId < 0) {
					final WAPI wapi = Shark.getInstance().getWAPIConnection();
					cmdbCardId = ((Long) wapi.getProcessInstanceAttributeValue(shandle, procInstId, "ProcessId")
							.getValue()).intValue();
				}

				param.the_value = cmdbCardId;
			}
		}
	}
}
