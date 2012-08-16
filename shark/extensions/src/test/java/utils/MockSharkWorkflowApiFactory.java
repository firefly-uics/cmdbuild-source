package utils;

import static org.mockito.Mockito.mock;

import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.api.SharkWorkflowApiFactory;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class MockSharkWorkflowApiFactory implements SharkWorkflowApiFactory {

	public static final FluentApiExecutor fluentApiExecutor;
	public static final Private proxy;

	static {
		fluentApiExecutor = mock(FluentApiExecutor.class);
		proxy = mock(Private.class);
	}

	@Override
	public void setup(final CallbackUtilities cus) {
		// nothing to do
	}

	@Override
	public void setup(final CallbackUtilities cus, final WMSessionHandle shandle, final String procInstId) {
		// nothing to do
	}

	@Override
	public WorkflowApi createWorkflowApi() {
		return new WorkflowApi(fluentApiExecutor, proxy);
	}

}
