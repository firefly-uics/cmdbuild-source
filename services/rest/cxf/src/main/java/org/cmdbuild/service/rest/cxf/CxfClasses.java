package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.size;

import java.util.Comparator;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.Classes;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.FullClassDetail;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleClassDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.service.rest.serialization.ErrorHandler;
import org.cmdbuild.service.rest.serialization.ToFullClassDetail;
import org.cmdbuild.service.rest.serialization.ToSimpleClassDetail;
import org.cmdbuild.workflow.user.UserProcessClass;

import com.google.common.collect.Ordering;

public class CxfClasses implements Classes {

	private static final ToFullClassDetail TO_FULL_CLASS_DETAIL = ToFullClassDetail.newInstance().build();
	private static final ToSimpleClassDetail TO_SIMPLE_CLASS_DETAIL = ToSimpleClassDetail.newInstance().build();

	private static final Comparator<CMClass> NAME_ASC = new Comparator<CMClass>() {

		@Override
		public int compare(final CMClass o1, final CMClass o2) {
			return o1.getName().compareTo(o2.getName());
		}

	};

	private final ErrorHandler errorHandler;
	private final DataAccessLogic userDataAccessLogic;
	private final WorkflowLogic userWorkflowLogic;

	public CxfClasses(final ErrorHandler errorHandler, final DataAccessLogic userDataAccessLogic,
			final WorkflowLogic userWorkflowLogic) {
		this.errorHandler = errorHandler;
		this.userDataAccessLogic = userDataAccessLogic;
		this.userWorkflowLogic = userWorkflowLogic;
	}

	@Override
	public ListResponse<SimpleClassDetail> readAll(final boolean activeOnly, final Integer limit, final Integer offset) {
		// FIXME do all the following it within the same logic
		// <<<<<
		final Iterable<? extends CMClass> allClasses = userDataAccessLogic.findClasses(activeOnly);
		final Iterable<? extends UserProcessClass> allProcessClasses = userWorkflowLogic.findProcessClasses(activeOnly);
		final Iterable<? extends CMClass> ordered = Ordering.from(NAME_ASC) //
				.sortedCopy(concat( //
						allClasses, //
						allProcessClasses));
		final Iterable<SimpleClassDetail> elements = from(ordered) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(TO_SIMPLE_CLASS_DETAIL);
		// <<<<<
		return ListResponse.<SimpleClassDetail> newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(Long.valueOf(size(ordered))) //
						.build()) //
				.build();
	}

	@Override
	public SimpleResponse<FullClassDetail> read(final String name) {
		final CMClass found = userDataAccessLogic.findClass(name);
		if (found == null) {
			errorHandler.classNotFound(name);
		}
		final FullClassDetail element = TO_FULL_CLASS_DETAIL.apply(found);
		return SimpleResponse.<FullClassDetail> newInstance() //
				.withElement(element) //
				.build();
	}

}
