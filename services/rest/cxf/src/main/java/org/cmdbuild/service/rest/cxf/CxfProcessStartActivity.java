package org.cmdbuild.service.rest.cxf;

import static org.cmdbuild.service.rest.dto.Builders.newResponseSingle;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.ProcessStartActivity;
import org.cmdbuild.service.rest.cxf.serialization.ToProcessActivityDefinition;
import org.cmdbuild.service.rest.dto.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.dto.ResponseSingle;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessClass;

public class CxfProcessStartActivity implements ProcessStartActivity {

	private static final CMActivity UNSUPPORTED_ACTIVITY = UnsupportedProxyFactory.of(CMActivity.class).create();

	private static final ToProcessActivityDefinition TO_PROCESS_ACTIVITY = ToProcessActivityDefinition.newInstance() //
			.build();

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;

	public CxfProcessStartActivity(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public ResponseSingle<ProcessActivityWithFullDetails> read(final String type) {
		final UserProcessClass found = workflowLogic.findProcessClass(type);
		if (found == null) {
			errorHandler.processNotFound(type);
		}
		final CMActivity activity = startActivityFor(type);
		final ProcessActivityWithFullDetails element = TO_PROCESS_ACTIVITY.apply(activity);
		return newResponseSingle(ProcessActivityWithFullDetails.class) //
				.withElement(element) //
				.build();
	}

	private CMActivity startActivityFor(final String type) {
		try {
			return workflowLogic.getStartActivity(type);
		} catch (final CMWorkflowException e) {
			errorHandler.propagate(e);
			return UNSUPPORTED_ACTIVITY;
		}
	}

}
