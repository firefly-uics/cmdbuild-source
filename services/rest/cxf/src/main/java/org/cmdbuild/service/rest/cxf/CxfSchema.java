package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.Schema;
import org.cmdbuild.service.rest.dto.ClassDetail;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Function;

public class CxfSchema implements Schema {

	private static Function<CMClass, ClassDetail> TO_CLASS_DETAILS = new Function<CMClass, ClassDetail>() {

		@Override
		public ClassDetail apply(final CMClass input) {
			return ClassDetail.newInstance() //
					.withName(input.getName()) //
					.withDescription(input.getDescription()) //
					.build();
		}

	};

	private static final boolean ACTIVE_ONLY = true;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public ClassDetailResponse getClasses() {
		final Iterable<? extends CMClass> allClasses = dataAccessLogic().findClasses(ACTIVE_ONLY);
		final Iterable<? extends UserProcessClass> allProcessClasses = workflowLogic().findProcessClasses(ACTIVE_ONLY);

		final Iterable<ClassDetail> details = from(concat(allClasses, allProcessClasses)) //
				.transform(TO_CLASS_DETAILS);
		return ClassDetailResponse.newInstance() //
				.withDetails(details) //
				.withTotal(size(details)) //
				.build();
	}

	private DataAccessLogic dataAccessLogic() {
		return applicationContext().getBean(SystemDataAccessLogicBuilder.class).build();
	}

	private WorkflowLogic workflowLogic() {
		return applicationContext().getBean(SystemWorkflowLogicBuilder.class).build();
	}

}
