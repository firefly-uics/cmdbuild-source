package org.cmdbuild.shark.toolagent;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;

import java.io.IOException;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.toolagent.AbstractToolAgent;
import org.enhydra.shark.toolagent.BshToolAgent;
import org.enhydra.shark.xpdl.XPDLConstants;
import org.enhydra.shark.xpdl.elements.ExtendedAttribute;
import org.enhydra.shark.xpdl.elements.ExtendedAttributes;

public class GroovyToolAgent extends AbstractToolAgent {

	private String script;
	private static final String GROOVY_SCRIPT_REPOSITORY = "CMDBuild.Groovy.Repository";

	public void invokeApplication(WMSessionHandle shandle, long handle,
			WMEntity appInfo, WMEntity toolInfo, String applicationName,
			String procInstId, String assId, AppParameter[] parameters,
			Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {

		super.invokeApplication(shandle, handle, appInfo, toolInfo, applicationName, procInstId, assId, parameters, appMode);

		try {
			status = APP_STATUS_RUNNING;
			// if appMode is set, execute groovy file, otherwise execute script
			if (appName != null && appName.trim().length() > 0) {
				if (appMode != null && appMode == BshToolAgent.APP_MODE_FILE) {
					executeGroovyFile(parameters, appName);
				} else {
					script = appName;
				}
			} else {
				readParamsFromExtAttributes((String) parameters[0].the_value);
				Binding binding = new Binding();
				setBindVariables(binding, parameters);
				GroovyShell shell = new GroovyShell(binding);
				shell.evaluate(script);
				getResultFromEvaluation(parameters, binding);
			}
			status = APP_STATUS_FINISHED;
		} catch (Throwable ex) {
			status = APP_STATUS_INVALID;
			cus.error(shandle,
					String.format("GroovyToolAgent - application %s terminated incorrectly with error %s", appName, ex.getMessage()));
			//TODO delete this
			ex.printStackTrace();
			throw new ToolAgentGeneralException(ex);
		}
	}

	private void executeGroovyFile(AppParameter[] parameters, String groovyFile) throws ApplicationNotDefined, ToolAgentGeneralException {
		String[] groovyRepository = new String[] { cus.getProperty(GROOVY_SCRIPT_REPOSITORY) };
		try {
			GroovyScriptEngine gse = new GroovyScriptEngine(groovyRepository);
			Binding binding = new Binding();
			setBindVariables(binding, parameters);
			gse.run(groovyFile, binding);
			getResultFromEvaluation(parameters, binding);
		} catch (IOException e) {
			cus.error(shandle, String.format( "GroovyToolAgent - application %s terminated incorrectly, can't find script file %s", appName, groovyFile), e);
			throw new ApplicationNotDefined(String.format("Can't find script file $s", appName), e);
		} catch (Throwable e) {
			cus.error(shandle, String.format("GroovyToolAgent - application %s terminated incorrectly: %s", appName, e));
			//TODO delete this
			e.printStackTrace();
			throw new ToolAgentGeneralException(e);
		}

	}

	private void getResultFromEvaluation(AppParameter[] parameters, Binding binding) throws Throwable {
		if (parameters != null) {
			for (AppParameter parameter : parameters) {
				// Get output formal parameters
				if (parameter.the_mode.equals(XPDLConstants.FORMAL_PARAMETER_MODE_OUT) || parameter.the_mode.equals(XPDLConstants.FORMAL_PARAMETER_MODE_INOUT)) {
					parameter.the_value = binding.getVariable(parameter.the_formal_name);
					// Conversion to maintain Shark compatibility
					if (parameter.the_value instanceof Integer) {
						parameter.the_value = new Long(((Integer) parameter.the_value).intValue());
					}
				}
			}
		}
	}

	private void setBindVariables(Binding binding, AppParameter[] parameters) throws Throwable {
		if (parameters != null) {
			// Shark workaround: ignore 1st param because "it is ext. attribs"
			for (AppParameter parameter : parameters) {
				// Getting formal parameters (input parameters)
				String key = parameter.the_formal_name;
				java.lang.Object value = parameter.the_value;
				binding.setVariable(key, value);
			}
		}
	}

	// This method comes from org.enhydra.shark.toolagent.BshToolAgent. It's a
	// cut&paste
	protected ExtendedAttributes readParamsFromExtAttributes(String extAttribs) throws Exception {
		ExtendedAttributes eas = super.readParamsFromExtAttributes(extAttribs);
		if (appName == null || appName.trim().length() == 0) {
			ExtendedAttribute ea = eas.getFirstExtendedAttributeForName(BshToolAgent.SCRIPT_EXT_ATTR_NAME);
			if (ea != null) {
				script = ea.getVValue();
			}
		}
		return eas;
	}
}