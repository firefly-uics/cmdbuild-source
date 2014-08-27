package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.ProcessInstances;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.ProcessInstance;
import org.cmdbuild.service.rest.serialization.ErrorHandler;
import org.cmdbuild.service.rest.serialization.ToProcessInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;

import com.google.common.base.Function;

public class CxfProcessInstances implements ProcessInstances {

	private static final Function<UserProcessInstance, ProcessInstance> TO_PROCESS_INSTANCE = ToProcessInstance //
			.newInstance() //
			.build();

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;

	public CxfProcessInstances(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public ListResponse<ProcessInstance> readAll(final String name, final Integer limit, final Integer offset) {
		final UserProcessClass found = workflowLogic.findProcessClass(name);
		if (found == null) {
			errorHandler.classNotFound(name);
		}
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				// TODO filters
				// TODO sorters
				.build();
		final PagedElements<UserProcessInstance> elements = workflowLogic.query(name, queryOptions);
		return ListResponse.newInstance(ProcessInstance.class) //
				.withElements(from(elements) //
						.transform(TO_PROCESS_INSTANCE)) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(Long.valueOf(elements.totalSize())) //
						.build()) //
				.build();
	}

}
