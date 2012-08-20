package org.cmdbuild.workflow.api;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.cmdbuild.workflow.Constants.CURRENT_USER_VARIABLE;

import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.CusSoapProxyBuilder;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class SoapSharkWorkflowApiFactory implements SharkWorkflowApiFactory {

	private static class ProcessData {

		public final WMSessionHandle shandle;
		public final String procInstId;

		public ProcessData(final WMSessionHandle shandle, final String procInstId) {
			this.shandle = shandle;
			this.procInstId = procInstId;
		}

	}

	private CallbackUtilities cus;
	private ProcessData processData;

	@Override
	public void setup(final CallbackUtilities cus) {
		setup(cus, null);
	}

	@Override
	public void setup(final CallbackUtilities cus, final WMSessionHandle shandle, final String procInstId) {
		setup(cus, new ProcessData(shandle, procInstId));
	}

	private void setup(final CallbackUtilities cus, final ProcessData processData) {
		this.cus = cus;
		this.processData = processData;
	}

	@Override
	public WorkflowApi createWorkflowApi() {
		final Private proxy = proxy();
		final WsFluentApiExecutor executor = new WsFluentApiExecutor(proxy);
		final WorkflowApi workflowApi = new WorkflowApi(executor, proxy);
		return workflowApi;
	}

	private Private proxy() {
		return new CusSoapProxyBuilder(cus) //
				.withUsername(currentUserOrEmptyOnError()) //
				.build();
	}

	private String currentUserOrEmptyOnError() {
		if (processData != null) {
			return EMPTY;
		}

		try {
			final WMAttribute attribute = wapi().getProcessInstanceAttributeValue(processData.shandle,
					processData.procInstId, CURRENT_USER_VARIABLE);
			final Object value = attribute.getValue();
			final ReferenceType userReference = ReferenceType.class.cast(value);
			return userReference.getDescription();
		} catch (final Throwable e) {
			return EMPTY;
		}
	}

	private WAPI wapi() throws Exception {
		return Shark.getInstance().getWAPIConnection();
	}

}
