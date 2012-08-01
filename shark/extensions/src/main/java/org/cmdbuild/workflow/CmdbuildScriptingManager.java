package org.cmdbuild.workflow;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.scripting.Evaluator;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.scripting.StandardScriptingManager;

public class CmdbuildScriptingManager extends ForwardingScriptingManager {

	public CmdbuildScriptingManager() {
		super(new StandardScriptingManager());
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
	}

	@Override
	public Evaluator getEvaluator(final WMSessionHandle sessionHandle, final String name) throws Exception {
		final Evaluator evaluator = super.getEvaluator(sessionHandle, name);
		if (evaluator == null) {
			// TODO add Groovy evaluator
		}
		return evaluator;
	}

}
