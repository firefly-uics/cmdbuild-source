package org.cmdbuild.shark.toolagent;

import java.lang.reflect.Method;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;

/**
* this ToolAgent is called only if the ExtendedAttribute "Condition" evaluate to "true".
* It is a wrapper on a ClassToolAgent, so an "AppName" ExtendedAttribute has to be defined.
* @author Francesco Zanitti
*
*/
public class ConditionalClassToolAgent extends AbstractConditionalToolAgent {
    
    private static final String EXECUTION_METHOD_NAME = "execute";

	@Override
	protected void innerInvoke(WMSessionHandle shandle, long handle,
			WMEntity appInfo, WMEntity toolInfo, String applicationName,
			String procInstId, String assId, AppParameter[] parameters,
			Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {
		try {
			makeCall(parameters);
		} catch (Exception e) {
			e.printStackTrace();
			status = APP_STATUS_INVALID;
			throw new ToolAgentGeneralException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void makeCall(AppParameter[] parameters) throws Exception {
        if (appName == null || appName.trim().length() == 0) {
            readParamsFromExtAttributes((String) parameters[0].the_value);
        }

        ClassLoader cl = getClass().getClassLoader();
        Class c = cl.loadClass(appName);

        // Get parameter types - ignore 1. param, these are ext.attribs
        Class[] parameterTypes = null;
        if (parameters != null) {
            parameterTypes = new Class[parameters.length - 1];
            for (int i = 1; i < parameters.length; i++) {
                parameterTypes[i - 1] = AppParameter.class;
            }
        }

        Method m = c.getMethod(EXECUTION_METHOD_NAME, parameterTypes);

        // invoke the method
        AppParameter[] aps = new AppParameter[parameters.length - 1];
        System.arraycopy(parameters, 1, aps, 0, aps.length);
        m.invoke(null, (Object[])aps);
    }
}
