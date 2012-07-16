package org.cmdbuild.workflow.api;

import org.enhydra.shark.api.internal.working.CallbackUtilities;

public abstract class SharkWorkflowApi implements WorkflowApi, SchemaApi {

	private CallbackUtilities cus;

	public void configure(final CallbackUtilities cus) {
		this.cus = cus;
	}

	protected CallbackUtilities cus() {
		return cus;
	}

}
