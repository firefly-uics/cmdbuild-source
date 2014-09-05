package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.transformEntries;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.FilterElementGetters;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.ProcessInstances;
import org.cmdbuild.service.rest.cxf.serialization.ToProcessInstance;
import org.cmdbuild.service.rest.cxf.util.Maps;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessInstance;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Maps.EntryTransformer;

public class CxfProcessInstances implements ProcessInstances {

	private static final EntryTransformer<String, List<? extends String>, String> FIRST_ELEMENT = Maps.firstElement();

	private static Map<String, Object> NO_WIDGET_SUBMISSION = Collections.emptyMap();

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;

	public CxfProcessInstances(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public SimpleResponse<Long> create(final String type, final MultivaluedMap<String, String> formParams,
			final boolean advance) {
		final UserProcessClass found = workflowLogic.findProcessClass(type);
		if (found == null) {
			errorHandler.processNotFound(type);
		}
		final Map<String, String> vars = transformEntries(formParams, FIRST_ELEMENT);
		try {
			final UserProcessInstance instance = workflowLogic.startProcess(type, vars, NO_WIDGET_SUBMISSION, advance);
			return SimpleResponse.newInstance(Long.class) //
					.withElement(instance.getId()) //
					.build();
		} catch (final Throwable e) {
			errorHandler.propagate(e);
		}
		return null;
	}

	@Override
	public SimpleResponse<ProcessInstance> read(final String type, final Long id) {
		final UserProcessClass found = workflowLogic.findProcessClass(type);
		if (found == null) {
			errorHandler.processNotFound(type);
		}
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(1) //
				.offset(0) //
				.filter(filterFor(id)) //
				.build();
		final PagedElements<UserProcessInstance> elements = workflowLogic.query(type, queryOptions);
		if (elements.totalSize() == 0) {
			errorHandler.processInstanceNotFound(id);
		}
		final Function<UserProcessInstance, ProcessInstance> toProcessInstance = ToProcessInstance.newInstance() //
				.withType(found) //
				.build();
		return SimpleResponse.newInstance(ProcessInstance.class) //
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
	public ListResponse<ProcessInstance> read(final String type, final Integer limit, final Integer offset) {
		final UserProcessClass found = workflowLogic.findProcessClass(type);
		if (found == null) {
			errorHandler.processNotFound(type);
		}
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				// TODO filters
				// TODO sorters
				.build();
		final PagedElements<UserProcessInstance> elements = workflowLogic.query(type, queryOptions);
		final Function<UserProcessInstance, ProcessInstance> toProcessInstance = ToProcessInstance.newInstance() //
				.withType(found) //
				.build();
		return ListResponse.newInstance(ProcessInstance.class) //
				.withElements(from(elements) //
						.transform(toProcessInstance)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(Long.valueOf(elements.totalSize())) //
						.build()) //
				.build();
	}

	@Override
	public void update(final String type, final Long id, final String activity, final boolean advance,
			final MultivaluedMap<String, String> formParams) {
		final UserProcessClass found = workflowLogic.findProcessClass(type);
		if (found == null) {
			errorHandler.processNotFound(type);
		}
		final Map<String, String> vars = transformEntries(formParams, FIRST_ELEMENT);
		try {
			workflowLogic.updateProcess(type, id, activity, vars, NO_WIDGET_SUBMISSION, advance);
		} catch (final Throwable e) {
			errorHandler.propagate(e);
		}
	}

}
