package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.size;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.service.rest.Classes;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.AttributeValueDetail;
import org.cmdbuild.service.rest.dto.AttributeValueDetailResponse;
import org.cmdbuild.service.rest.dto.CardDetail;
import org.cmdbuild.service.rest.dto.CardDetailResponse;
import org.cmdbuild.service.rest.dto.ClassDetail;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;
import org.cmdbuild.service.rest.serialization.AttributeTypeResolver;
import org.cmdbuild.service.rest.serialization.ToAttributeDetail;
import org.cmdbuild.service.rest.serialization.ToAttributeValueDetail;
import org.cmdbuild.service.rest.serialization.ToCardDetail;
import org.cmdbuild.service.rest.serialization.ToClassDetail;
import org.cmdbuild.workflow.user.UserProcessClass;

import com.mchange.util.AssertException;

public class CxfClasses extends CxfService implements Classes {

	private static final ToClassDetail TO_CLASS_DETAIL = ToClassDetail.newInstance().build();
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
	public CardDetailResponse getCards(final String name) {
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(Integer.MAX_VALUE) //
				.offset(0) //
				.build();
		final FetchCardListResponse response = userDataAccessLogic().fetchCards(name, queryOptions);

		final ToCardDetail toCardDetail = ToCardDetail.newInstance() //
				.withDataView(systemDataView()) //
				.withErrorHandler(errorHandler()) //
				.build();
		final Iterable<CardDetail> details = from(response.elements()) //
				.transform(toCardDetail);
		return CardDetailResponse.newInstance() //
				.withDetails(details) //
				.withTotal(response.getTotalNumberOfCards()) //
				.build();
	}

	@Override
	public AttributeValueDetailResponse getAttributes(final String name, final Long id) {
		// TODO inject error management within logic
		if (userDataView().findClass(name) == null) {
			errorHandler().classNotFound(name);
		}
		try {
			final CMCard fetched = userDataAccessLogic().fetchCMCard(name, id);

			final ToAttributeValueDetail toAttributeValueDetail = ToAttributeValueDetail.newInstance() //
					.with(fetched.getType()) //
					.build();
			final Iterable<AttributeValueDetail> details = from(fetched.getAllValues()) //
					.transform(toAttributeValueDetail);
			return AttributeValueDetailResponse.newInstance() //
					.withDetails(details) //
					.withTotal(size(details)) //
					.build();
		} catch (final NotFoundException e) {
			errorHandler().cardNotFound(id);
			throw new AssertException("should not come here");
		}

	}

}
