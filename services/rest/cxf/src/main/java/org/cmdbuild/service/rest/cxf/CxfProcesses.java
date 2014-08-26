package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;

import java.util.Comparator;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.Processes;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.FullProcessDetail;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleProcessDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.service.rest.serialization.ErrorHandler;
import org.cmdbuild.service.rest.serialization.ToFullProcessDetail;
import org.cmdbuild.service.rest.serialization.ToSimpleProcessDetail;
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
	public ListResponse<SimpleProcessDetail> readAll(final boolean activeOnly, final Integer limit, final Integer offset) {
		final Iterable<? extends UserProcessClass> all = workflowLogic.findProcessClasses(activeOnly);
		final Iterable<? extends UserProcessClass> ordered = Ordering.from(NAME_ASC) //
				.sortedCopy(all);
		final Iterable<SimpleProcessDetail> elements = from(ordered) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(TO_SIMPLE_DETAIL);
		return ListResponse.<SimpleProcessDetail> newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(Long.valueOf(size(ordered))) //
						.build()) //
				.build();
	}

	@Override
	public SimpleResponse<FullProcessDetail> read(final String name) {
		final CMClass found = workflowLogic.findProcessClass(name);
		if (found == null) {
			errorHandler.classNotFound(name);
		}
		final FullProcessDetail element = TO_FULL_DETAIL.apply(found);
		return SimpleResponse.<FullProcessDetail> newInstance() //
				.withElement(element) //
				.build();
	}

}
