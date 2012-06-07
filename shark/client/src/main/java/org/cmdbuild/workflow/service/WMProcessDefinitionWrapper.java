package org.cmdbuild.workflow.service;

import org.enhydra.shark.api.client.wfmc.wapi.WMProcessDefinition;

public class WMProcessDefinitionWrapper implements WSProcessDefInfo {

	final WMProcessDefinition inner;

	WMProcessDefinitionWrapper(final WMProcessDefinition processDefinition) {
		this.inner = processDefinition;
	}

	@Override
	public String getProcessDefinitionId() {
		return inner.getId();
	}

}
