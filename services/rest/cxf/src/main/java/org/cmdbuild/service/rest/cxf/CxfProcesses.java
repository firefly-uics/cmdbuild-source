package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.service.rest.dto.Builders.newMetadata;
import static org.cmdbuild.service.rest.dto.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.dto.Builders.newResponseSingle;

import java.util.Comparator;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.Processes;
import org.cmdbuild.service.rest.cxf.serialization.ToFullProcessDetail;
import org.cmdbuild.service.rest.cxf.serialization.ToSimpleProcessDetail;
import org.cmdbuild.service.rest.dto.ProcessWithBasicDetails;
import org.cmdbuild.service.rest.dto.ProcessWithFullDetails;
import org.cmdbuild.service.rest.dto.ResponseMultiple;
import org.cmdbuild.service.rest.dto.ResponseSingle;
import org.cmdbuild.workflow.user.UserProcessClass;

import com.google.common.collect.Ordering;

public class CxfProcesses implements Processes {

	private static final ToSimpleProcessDetail TO_SIMPLE_DETAIL = ToSimpleProcessDetail.newInstance().build();
	private static final ToFullProcessDetail TO_FULL_DETAIL = ToFullProcessDetail.newInstance().build();

	private static final Comparator<CMClass> NAME_ASC = new Comparator<CMClass>() {

		@Override
		public int compare(final CMClass o1, final CMClass o2) {
			return o1.getName().compareTo(o2.getName());
		}

	};

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;

	public CxfProcesses(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public ResponseMultiple<ProcessWithBasicDetails> readAll(final boolean activeOnly, final Integer limit,
			final Integer offset) {
		final Iterable<? extends UserProcessClass> all = workflowLogic.findProcessClasses(activeOnly);
		final Iterable<? extends UserProcessClass> ordered = Ordering.from(NAME_ASC) //
				.sortedCopy(all);
		final Iterable<ProcessWithBasicDetails> elements = from(ordered) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(TO_SIMPLE_DETAIL);
		return newResponseMultiple(ProcessWithBasicDetails.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(ordered))) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<ProcessWithFullDetails> read(final String name) {
		final CMClass found = workflowLogic.findProcessClass(name);
		if (found == null) {
			errorHandler.classNotFound(name);
		}
		final ProcessWithFullDetails element = TO_FULL_DETAIL.apply(found);
		return newResponseSingle(ProcessWithFullDetails.class) //
				.withElement(element) //
				.build();
	}

}
