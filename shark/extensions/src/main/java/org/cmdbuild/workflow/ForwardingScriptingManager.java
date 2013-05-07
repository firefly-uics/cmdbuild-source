package org.cmdbuild.workflow;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.scripting.Evaluator;
import org.enhydra.shark.api.internal.scripting.ScriptingManager;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public abstract class ForwardingScriptingManager implements ScriptingManager {

	private final ScriptingManager inner;

	public ForwardingScriptingManager(final ScriptingManager scriptingManager) {
		this.inner = scriptingManager;
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		inner.configure(cus);
	}

	@Override
	public Evaluator getEvaluator(final WMSessionHandle sessionHandle, final String name) throws Exception {
		return inner.getEvaluator(sessionHandle, name);
	}

}
