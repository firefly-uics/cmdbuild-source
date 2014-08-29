package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.EQUAL;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper.FilterElementGetter;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.ProcessInstances;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessInstance;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.service.rest.serialization.ErrorHandler;
import org.cmdbuild.service.rest.serialization.ToProcessInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;

public class CxfProcessInstances implements ProcessInstances {

	private static class IdFilterElementGetter implements FilterElementGetter {

		public static IdFilterElementGetter of(final Long id) {
			return new IdFilterElementGetter(id);
		}

		private final Long id;

		private IdFilterElementGetter(final Long id) {
			this.id = id;
		}

		@Override
		public boolean hasElement() {
			return true;
		}

		@Override
		public JSONObject getElement() throws JSONException {
			logger.debug(marker, "creating JSON element for '{}'", id);
			final JSONObject element = new JSONObject();
			element.put(ATTRIBUTE_KEY, "Id");
			element.put(OPERATOR_KEY, EQUAL);
			element.put(VALUE_KEY, new JSONArray(asList(id)));
			logger.debug(marker, "resulting element is '{}'", element);
			return element;
		}

	}

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;

	public CxfProcessInstances(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
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
					.merge(IdFilterElementGetter.of(id));
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

}
