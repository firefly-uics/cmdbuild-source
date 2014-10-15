package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.FilterElementGetters;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.ProcessInstanceActivities;
import org.cmdbuild.service.rest.cxf.serialization.ToProcessActivity;
import org.cmdbuild.service.rest.cxf.serialization.ToProcessActivityDefinition;
import org.cmdbuild.service.rest.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicates;

public class CxfProcessInstanceActivities implements ProcessInstanceActivities {

	private static final ToProcessActivity TO_OUTPUT = ToProcessActivity.newInstance() //
			.build();
	private static final ToProcessActivityDefinition TO_PROCESS_ACTIVITY = ToProcessActivityDefinition.newInstance() //
			.build();

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;

	public CxfProcessInstanceActivities(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public ResponseMultiple<ProcessActivityWithBasicDetails> read(final String processId, final Long processInstanceId) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(1) //
				.offset(0) //
				.filter(filterFor(processInstanceId)) //
				.build();
		final PagedElements<UserProcessInstance> elements = workflowLogic.query(found, queryOptions);
		if (elements.totalSize() == 0) {
			errorHandler.processInstanceNotFound(processInstanceId);
		}
		final Iterable<UserActivityInstance> activities = getOnlyElement(elements).getActivities();
		return newResponseMultiple(ProcessActivityWithBasicDetails.class) //
				.withElements(from(activities) //
						.transform(TO_OUTPUT) //
				) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(activities))) //
						.build() //
				).build();
	}

	private JSONObject filterFor(final Long id) {
		try {
			final JSONObject emptyFilter = new JSONObject();
			return new JsonFilterHelper(emptyFilter) //
					.merge(FilterElementGetters.id(id));
		} catch (final JSONException e) {
			errorHandler.propagate(e);
			return new JSONObject();
		}
	}

	@Override
	public ResponseSingle<ProcessActivityWithFullDetails> read(final String processId, final Long processInstanceId,
			final String processActivityId) {
		final UserProcessClass foundType = workflowLogic.findProcessClass(processId);
		if (foundType == null) {
			errorHandler.processNotFound(processId);
		}
		final UserProcessInstance foundInstance = workflowLogic.getProcessInstance(processId, processInstanceId);
		if (foundInstance == null) {
			errorHandler.processInstanceNotFound(processInstanceId);
		}
		CMActivity foundActivity = null;
		for (final UserActivityInstance element : foundInstance.getActivities()) {
			if (element.getId().equals(processActivityId)) {
				try {
					foundActivity = element.getDefinition();
				} catch (final Throwable e) {
					errorHandler.propagate(e);
				}
			}
		}
		if (foundActivity == null) {
			errorHandler.processActivityNotFound(processActivityId);
		}
		return newResponseSingle(ProcessActivityWithFullDetails.class) //
				.withElement(from(asList(foundActivity)) //
						.filter(Predicates.notNull()) //
						.transform(TO_PROCESS_ACTIVITY) //
						.first() //
						.get()) //
				.build();
	}

}
