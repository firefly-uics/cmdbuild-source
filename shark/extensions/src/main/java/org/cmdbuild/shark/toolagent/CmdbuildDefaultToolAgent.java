package org.cmdbuild.shark.toolagent;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.workflow.ConfigurationHelper;
import org.cmdbuild.workflow.Constants;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.toolagent.DefaultToolAgent;

/**
 * A replacement for {@link DefaultToolAgent} class.
 *
 * This implementation:
 * <ul>
 * <li>is needed for Shark 4.4 only</li>
 * <li>supports Groovy scripts</li>
 * <li>injects CMDBuild APIs as script parameters</li>
 * <ul>
 */
public class CmdbuildDefaultToolAgent extends OverriddableDefaultToolAgent {

	private static final String TEXT_GROOVY = "text/groovy";

	private FluentApi fluentApi;

	@Override
	protected void configureOthers(final CallbackUtilities cus) throws Exception {
		fluentApi = initApi(cus);
	}

	protected FluentApi initApi(final CallbackUtilities cus) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		final ConfigurationHelper helper = new ConfigurationHelper(cus);
		return helper.newSharkWorkflowApi().fluentApi();
	}

	@Override
	protected String classNameForScriptType(final String scriptType) {
		final String className = super.classNameForScriptType(scriptType);
		if ((className == null) && TEXT_GROOVY.equals(scriptType)) {
			return GroovyToolAgent.class.getName();
		}
		return className;
	}

	@Override
	protected AppParameter[] parametersForInvocation(final AppParameter[] parameters) {
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

}
