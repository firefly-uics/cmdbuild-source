package org.cmdbuild.workflow.api;

import org.enhydra.shark.api.internal.working.CallbackUtilities;

public abstract class SharkWorkflowApi {

	private CallbackUtilities cus;

	public void configure(final CallbackUtilities cus) {
		this.cus = cus;
	}

	protected CallbackUtilities cus() {
		return cus;
	}

	public abstract WorkflowApi workflowApi();

	public abstract SchemaApi schemaApi();

}
