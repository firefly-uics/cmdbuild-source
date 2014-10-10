package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.service.rest.cxf.serialization.FakeId.fakeId;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;

import java.util.Collections;
import java.util.Map;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.FilterElementGetters;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.ProcessInstances;
import org.cmdbuild.service.rest.cxf.serialization.ToProcessInstance;
import org.cmdbuild.service.rest.model.ProcessInstance;
import org.cmdbuild.service.rest.model.ProcessInstanceAdvanceable;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CxfProcessInstances implements ProcessInstances {

	private static Map<String, Object> NO_WIDGET_SUBMISSION = Collections.emptyMap();

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;

	public CxfProcessInstances(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public ResponseSingle<Long> create(final Long processId, final ProcessInstanceAdvanceable processInstance) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		try {
			final UserProcessInstance instance = workflowLogic.startProcess( //
					processId, //
					processInstance.getValues(), //
					NO_WIDGET_SUBMISSION, //
					processInstance.isAdvance());
			return newResponseSingle(Long.class) //
					.withElement(instance.getId()) //
					.build();
		} catch (final Throwable e) {
			errorHandler.propagate(e);
		}
		return null;
	}

	@Override
	public ResponseSingle<ProcessInstance> read(final Long processId, final Long instanceId) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(1) //
				.offset(0) //
				.filter(filterFor(instanceId)) //
				.build();
		final PagedElements<UserProcessInstance> elements = workflowLogic.query(found.getName(), queryOptions);
		if (elements.totalSize() == 0) {
			errorHandler.processInstanceNotFound(instanceId);
		}
		final Function<UserProcessInstance, ProcessInstance> toProcessInstance = ToProcessInstance.newInstance() //
				.withType(found) //
				.build();
		return newResponseSingle(ProcessInstance.class) //
				.withElement(from(elements) //
						.transform(toProcessInstance) //
						.first() //
						.get()) //
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

	@Override
	public ResponseMultiple<ProcessInstance> read(final Long processId, final Integer limit, final Integer offset) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				// TODO filters
				// TODO sorters
				.build();
		final PagedElements<UserProcessInstance> elements = workflowLogic.query(found.getName(), queryOptions);
		final Function<UserProcessInstance, ProcessInstance> toProcessInstance = ToProcessInstance.newInstance() //
				.withType(found) //
				.build();
		return newResponseMultiple(ProcessInstance.class) //
				.withElements(from(elements) //
						.transform(toProcessInstance)) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(elements.totalSize())) //
						.build()) //
				.build();
	}

	@Override
	public void update(final Long processId, final Long instanceId, final ProcessInstanceAdvanceable processInstance) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		try {
			final UserProcessInstance instance = workflowLogic.getProcessInstance(processId, instanceId);
			final Optional<UserActivityInstance> activity = from(instance.getActivities()) //
					.filter(new Predicate<UserActivityInstance>() {

						@Override
						public boolean apply(final UserActivityInstance input) {
							return fakeId(input.getId()).equals(processInstance.getActivity());
						};

					}) //
					.first();
			if (!activity.isPresent()) {
				errorHandler.processActivityNotFound(processInstance.getActivity());
			}
			workflowLogic.updateProcess( //
					processId, //
					instanceId, //
					activity.get().getId(), //
					processInstance.getValues(), //
					NO_WIDGET_SUBMISSION, //
					processInstance.isAdvance());
		} catch (final Throwable e) {
			errorHandler.propagate(e);
		}
	}

	@Override
	public void delete(final Long processId, final Long instanceId) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		try {
			workflowLogic.abortProcess(processId, instanceId);
		} catch (final Throwable e) {
			errorHandler.propagate(e);
		}
	}

}
