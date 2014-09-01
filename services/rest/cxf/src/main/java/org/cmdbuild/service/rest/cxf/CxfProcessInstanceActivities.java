package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.getOnlyElement;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.FilterElementGetters;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.ProcessInstanceActivities;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessActivityShort;
import org.cmdbuild.service.rest.serialization.ErrorHandler;
import org.cmdbuild.service.rest.serialization.ToProcessActivityShort;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.json.JSONException;
import org.json.JSONObject;

public class CxfProcessInstanceActivities implements ProcessInstanceActivities {

	private static final ToProcessActivityShort TO_OUTPUT = ToProcessActivityShort.newInstance() //
			.build();

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;

	public CxfProcessInstanceActivities(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public ListResponse<ProcessActivityShort> read(final String type, final Long instance) {
		final UserProcessClass found = workflowLogic.findProcessClass(type);
		if (found == null) {
			errorHandler.processNotFound(type);
		}
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(1) //
				.offset(0) //
				.filter(filterFor(instance)) //
				.build();
		final PagedElements<UserProcessInstance> elements = workflowLogic.query(type, queryOptions);
		if (elements.totalSize() == 0) {
			errorHandler.processInstanceNotFound(instance);
		}
		final Iterable<UserActivityInstance> activities = getOnlyElement(elements).getActivities();
		return ListResponse.newInstance(ProcessActivityShort.class) //
				.withElements(from(activities) //
						.transform(TO_OUTPUT) //
				) //
				.build();
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

}
