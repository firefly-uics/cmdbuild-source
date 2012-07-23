package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.FluentApi;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public abstract class SharkWorkflowApi {

	private CallbackUtilities cus;

	public void configure(final CallbackUtilities cus) {
		this.cus = cus;
	}

	protected CallbackUtilities cus() {
		return cus;
	}

	public abstract FluentApi fluentApi();

	public abstract SchemaApi schemaApi();

	public abstract WorkflowApi workflowApi();

}
