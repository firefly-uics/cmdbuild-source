package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.size;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.service.rest.Schema;
import org.cmdbuild.service.rest.dto.schema.AttributeDetail;
import org.cmdbuild.service.rest.dto.schema.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.schema.ClassDetail;
import org.cmdbuild.service.rest.dto.schema.ClassDetailResponse;
import org.cmdbuild.service.rest.dto.schema.LookupDetail;
import org.cmdbuild.service.rest.dto.schema.LookupDetailResponse;
import org.cmdbuild.service.rest.dto.schema.LookupTypeDetail;
import org.cmdbuild.service.rest.dto.schema.LookupTypeDetailResponse;
import org.cmdbuild.service.rest.serialization.schema.AttributeTypeResolver;
import org.cmdbuild.service.rest.serialization.schema.ToAttributeDetail;
import org.cmdbuild.service.rest.serialization.schema.ToClassDetail;
import org.cmdbuild.service.rest.serialization.schema.ToLookupDetail;
import org.cmdbuild.service.rest.serialization.schema.ToLookupTypeDetail;
import org.cmdbuild.workflow.user.UserProcessClass;

import com.google.common.collect.Lists;

public class CxfSchema extends CxfService implements Schema {

	private static final ToClassDetail TO_CLASS_DETAIL = ToClassDetail.newInstance().build();
	private static final ToLookupTypeDetail TO_LOOKUP_TYPE_DETAIL = ToLookupTypeDetail.newInstance().build();
	private static final ToLookupDetail TO_LOOKUP_DETAIL = ToLookupDetail.newInstance().build();
	private static final AttributeTypeResolver ATTRIBUTE_TYPE_RESOLVER = new AttributeTypeResolver();

	@Override
	public ClassDetailResponse getClasses(final boolean activeOnly) {
		final Iterable<? extends CMClass> allClasses = userDataAccessLogic().findClasses(activeOnly);
		final Iterable<? extends UserProcessClass> allProcessClasses = userWorkflowLogic().findProcessClasses(
				activeOnly);

		final Iterable<ClassDetail> details = from(concat(allClasses, allProcessClasses)) //
				.transform(TO_CLASS_DETAIL);
		return ClassDetailResponse.newInstance() //
				.withDetails(details) //
				.withTotal(size(details)) //
				.build();
	}

	@Override
	public AttributeDetailResponse getAttributes(final String name, final boolean activeOnly) {
		final CMClass target = userDataAccessLogic().findClass(name);
		if (target == null) {
			throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
					.entity(name) //
					.build());
		}
		final Iterable<? extends CMAttribute> attributes = userDataAccessLogic().getAttributes(name, activeOnly);

		final ToAttributeDetail toAttributeDetails = ToAttributeDetail.newInstance() //
				.withAttributeTypeResolver(ATTRIBUTE_TYPE_RESOLVER) //
				.withDataView(systemDataView()) //
				.withErrorHandler(errorHandler()) //
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

}
