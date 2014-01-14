package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.Schema;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
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

	private static Function<CMAttribute, AttributeDetail> TO_ATTRIBUTE_DETAILS = new Function<CMAttribute, AttributeDetail>() {

		@Override
		public AttributeDetail apply(final CMAttribute input) {
			return AttributeDetail.newInstance() //
					.withName(input.getName()) //
					.withDescription(input.getDescription()) //
					.thatIsDisplayableInList(input.isDisplayableInList()) //
					.thatIsUnique(input.isUnique()) //
					.thatIsMandatory(input.isMandatory()) //
					.thatIsInherited(input.isInherited()) //
					.thatIsActive(input.isActive()) //
					.withIndex(input.getIndex()) //
					.withDefaultValue(input.getDefaultValue()) //
					.withGroup(input.getGroup()) //
					.build();
		}

	};

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public ClassDetailResponse getClasses(final boolean activeOnly) {
		final Iterable<? extends CMClass> allClasses = dataAccessLogic().findClasses(activeOnly);
		final Iterable<? extends UserProcessClass> allProcessClasses = workflowLogic().findProcessClasses(activeOnly);

		final Iterable<ClassDetail> details = from(concat(allClasses, allProcessClasses)) //
				.transform(TO_CLASS_DETAILS);
		return ClassDetailResponse.newInstance() //
				.withDetails(details) //
				.withTotal(size(details)) //
				.build();
	}

	@Override
	public AttributeDetailResponse getAttributes(final String name, final boolean activeOnly) {
		final CMClass target = dataAccessLogic().findClass(name);
		if (target == null) {
			throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
					.entity(name) //
					.build());
		}
		final Iterable<? extends CMAttribute> attributes = dataAccessLogic().getAttributes(name, activeOnly);

		final Iterable<AttributeDetail> details = from(attributes) //
				.transform(TO_ATTRIBUTE_DETAILS);
		return AttributeDetailResponse.newInstance() //
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
