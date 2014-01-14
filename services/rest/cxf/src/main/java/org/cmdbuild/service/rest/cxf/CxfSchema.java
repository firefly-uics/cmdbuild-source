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
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.Schema;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.ClassDetail;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;
import org.cmdbuild.service.rest.dto.LookupDetail;
import org.cmdbuild.service.rest.dto.LookupDetailResponse;
import org.cmdbuild.service.rest.dto.LookupTypeDetail;
import org.cmdbuild.service.rest.dto.LookupTypeDetailResponse;
import org.cmdbuild.service.rest.serialization.AttributeTypeResolver;
import org.cmdbuild.service.rest.serialization.ToAttributeDetail;
import org.cmdbuild.service.rest.serialization.ToAttributeDetail.ErrorHandler;
import org.cmdbuild.service.rest.serialization.ToClassDetail;
import org.cmdbuild.service.rest.serialization.ToLookupDetail;
import org.cmdbuild.service.rest.serialization.ToLookupTypeDetail;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;

public class CxfSchema implements Schema {

	private static final ToAttributeDetail.ErrorHandler ERROR_HANDLER = new ErrorHandler() {

		@Override
		public void domainNotFound(final String domainName) {
			throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
					.entity(domainName) //
					.build());
		}

	};

	private static final ToClassDetail TO_CLASS_DETAIL = ToClassDetail.newInstance().build();
	private static final ToLookupTypeDetail TO_LOOKUP_TYPE_DETAIL = ToLookupTypeDetail.newInstance().build();
	private static final ToLookupDetail TO_LOOKUP_DETAIL = ToLookupDetail.newInstance().build();
	private static final AttributeTypeResolver ATTRIBUTE_TYPE_RESOLVER = new AttributeTypeResolver();

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public ClassDetailResponse getClasses(final boolean activeOnly) {
		final Iterable<? extends CMClass> allClasses = dataAccessLogic().findClasses(activeOnly);
		final Iterable<? extends UserProcessClass> allProcessClasses = workflowLogic().findProcessClasses(activeOnly);

		final Iterable<ClassDetail> details = from(concat(allClasses, allProcessClasses)) //
				.transform(TO_CLASS_DETAIL);
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

		final ToAttributeDetail toAttributeDetails = ToAttributeDetail.newInstance() //
				.withAttributeTypeResolver(ATTRIBUTE_TYPE_RESOLVER) //
				.withDataView(systemDataView()) //
				.withErrorHandler(ERROR_HANDLER) //
				.build();
		final Iterable<AttributeDetail> details = from(attributes) //
				.transform(toAttributeDetails);
		return AttributeDetailResponse.newInstance() //
				.withDetails(details) //
				.withTotal(size(details)) //
				.build();
	}

	@Override
	public LookupTypeDetailResponse getLookupTypes() {
		final Iterable<? extends LookupType> lookupTypes = Lists.newArrayList(lookupLogic().getAllTypes());

		final Iterable<LookupTypeDetail> details = from(lookupTypes) //
				.transform(TO_LOOKUP_TYPE_DETAIL);
		return LookupTypeDetailResponse.newInstance() //
				.withDetails(details) //
				.withTotal(size(details)) //
				.build();
	}

	@Override
	public LookupDetailResponse getLookups(final String type, final boolean activeOnly) {
		final LookupType lookupType = LookupType.newInstance().withName(type).build();
		final Iterable<? extends Lookup> lookups = lookupLogic().getAllLookup(lookupType, activeOnly);

		final Iterable<LookupDetail> details = from(lookups) //
				.transform(TO_LOOKUP_DETAIL);
		return LookupDetailResponse.newInstance() //
				.withDetails(details) //
				.withTotal(size(details)) //
				.build();
	}

	private DataAccessLogic dataAccessLogic() {
		return applicationContext().getBean(SystemDataAccessLogicBuilder.class).build();
	}

	private LookupLogic lookupLogic() {
		return applicationContext().getBean(LookupLogic.class);
	}

	private WorkflowLogic workflowLogic() {
		return applicationContext().getBean(SystemWorkflowLogicBuilder.class).build();
	}

	private CMDataView systemDataView() {
		return applicationContext().getBean(DBDataView.class);
	}

}
