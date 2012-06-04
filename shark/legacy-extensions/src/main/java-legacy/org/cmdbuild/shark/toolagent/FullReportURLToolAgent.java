package org.cmdbuild.shark.toolagent;

import org.cmdbuild.shark.util.CmdbuildUtils;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;

/**
 * This toolagent simply complete the url of a report file with the cmdbuild url.
 * The input string is a file name returned by a createReport extended attribute
 * @author zanitti
 *
 */
public class FullReportURLToolAgent extends AbstractConditionalToolAgent {

	@Override
	protected void innerInvoke(WMSessionHandle shandle, long handle,
			WMEntity appInfo, WMEntity toolInfo, String applicationName,
			String procInstId, String assId, AppParameter[] parameters,
			Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {

		String cmdbUri = CmdbuildUtils.getInstance().getCmdbuildEndpoint();
		
		String fileName = (String)parameters[1].the_value;
		
		parameters[1].the_value = cmdbUri + "report/" + fileName;
		
		System.out.println("Full report URL: " + parameters[1].the_value);
	}

}
