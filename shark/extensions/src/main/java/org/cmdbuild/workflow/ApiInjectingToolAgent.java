package org.cmdbuild.workflow;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.api.fluent.FluentApi;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.toolagent.DefaultToolAgent;

public class ApiInjectingToolAgent extends DefaultToolAgent {

	private FluentApi fluentApi;

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		fluentApi = initApi(cus);
	}

	protected FluentApi initApi(final CallbackUtilities cus) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		final ConfigurationHelper helper = new ConfigurationHelper(cus);
		return helper.newSharkWorkflowApi().fluentApi();
	}

	@Override
	public void invokeApplication(final WMSessionHandle shandle, final long handle, final WMEntity appInfo,
			final WMEntity toolInfo, final String applicationName, final String procInstId, final String assId,
			final AppParameter[] parameters, final Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {
		super.invokeApplication( //
				shandle, //
				handle, //
				appInfo, //
				toolInfo, //
				applicationName, //
				procInstId, //
				assId, //
				injectApi(parameters), //
				appMode);
	}

	private AppParameter[] injectApi(final AppParameter[] parameters) {
		final List<AppParameter> appParameters = new ArrayList<AppParameter>(asList(parameters));
		appParameters.add(apiParameter());
		return appParameters.toArray(new AppParameter[appParameters.size()]);
	}

	private AppParameter apiParameter() {
		return new AppParameter( //
				Constants.API_VARIABLE, //
				Constants.API_VARIABLE, //
				XPDLConstants.FORMAL_PARAMETER_MODE_IN, //
				fluentApi, //
				FluentApi.class);
	}

	// TODO override readParamsFromExtAttributes for adding Groovy tool agent

}
