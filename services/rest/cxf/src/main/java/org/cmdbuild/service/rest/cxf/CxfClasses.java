package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.size;

import java.util.Comparator;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.service.rest.Classes;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.CardListResponse;
import org.cmdbuild.service.rest.dto.CardResponse;
import org.cmdbuild.service.rest.dto.ClassDetail;
import org.cmdbuild.service.rest.dto.ClassDetailResponse;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.serialization.AttributeTypeResolver;
import org.cmdbuild.service.rest.serialization.FromCMCardToCardDetail;
import org.cmdbuild.service.rest.serialization.FromCardToCardDetail;
import org.cmdbuild.service.rest.serialization.FromSomeKindOfCardToMap;
import org.cmdbuild.service.rest.serialization.ToAttributeDetail;
import org.cmdbuild.service.rest.serialization.ToClassDetail;
import org.cmdbuild.workflow.user.UserProcessClass;

import com.google.common.collect.Ordering;
import com.mchange.util.AssertException;

public class CxfClasses extends CxfService implements Classes {

	private static final ToClassDetail TO_CLASS_DETAIL = ToClassDetail.newInstance().build();
	private static final AttributeTypeResolver ATTRIBUTE_TYPE_RESOLVER = new AttributeTypeResolver();

	private static final Comparator<CMClass> NAME_ASC = new Comparator<CMClass>() {

		@Override
		public int compare(final CMClass o1, final CMClass o2) {
			return o1.getName().compareTo(o2.getName());
		}

	};

	@Override
	public ClassDetailResponse getClasses(final boolean activeOnly, final Integer limit, final Integer offset) {
		// FIXME do all the following it within the same logic
		// <<<<<
		final Iterable<? extends CMClass> allClasses = userDataAccessLogic().findClasses(activeOnly);
		final Iterable<? extends UserProcessClass> allProcessClasses = userWorkflowLogic().findProcessClasses(
				activeOnly);
		final Iterable<? extends CMClass> ordered = Ordering.from(NAME_ASC) //
				.sortedCopy(concat( //
						allClasses, //
						allProcessClasses));
		final Iterable<ClassDetail> elements = from(ordered) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(TO_CLASS_DETAIL);
		// <<<<<
		return ClassDetailResponse.newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(size(elements)) //
						.build()) //
				.build();
	}

	@Override
	public AttributeDetailResponse getAttributes(final String name, final boolean activeOnly, final Integer limit,
			final Integer offset) {
		final CMClass target = userDataAccessLogic().findClass(name);
		if (target == null) {
			throw new WebApplicationException(Response.status(Status.NOT_FOUND) //
					.entity(name) //
					.build());
		}
		final PagedElements<CMAttribute> filteredAttributes = userDataAccessLogic().getAttributes( //
				name, //
				activeOnly, //
				new AttributesQuery() {

					@Override
					public Integer limit() {
						return limit;
					}

					@Override
					public Integer offset() {
						return offset;
					}

				});

		final ToAttributeDetail toAttributeDetails = ToAttributeDetail.newInstance() //
				.withAttributeTypeResolver(ATTRIBUTE_TYPE_RESOLVER) //
				.withDataView(systemDataView()) //
				.withErrorHandler(errorHandler()) //
				.build();
		final Iterable<AttributeDetail> elements = from(filteredAttributes) //
				.transform(toAttributeDetails);
		return AttributeDetailResponse.newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(filteredAttributes.totalSize()) //
						.build()) //
				.build();
	}

	@Override
	public CardListResponse getCards(final String name, final Integer limit, final Integer offset) {
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.build();
		final FetchCardListResponse response = userDataAccessLogic().fetchCards(name, queryOptions);

		final FromSomeKindOfCardToMap<Card> toCardDetail = FromCardToCardDetail.newInstance() //
				.withDataView(systemDataView()) //
				.withErrorHandler(errorHandler()) //
				.build();
		final Iterable<Map<String, Object>> elements = from(response.elements()) //
				.transform(toCardDetail);
		return CardListResponse.newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(response.getTotalNumberOfCards()) //
						.build()) //
				.build();
	}

	@Override
	public CardResponse getCard(final String name, final Long id) {
		// TODO inject error management within logic
		if (userDataView().findClass(name) == null) {
			errorHandler().classNotFound(name);
		}
		try {
			final CMCard fetched = userDataAccessLogic().fetchCMCard(name, id);
			final Map<String, Object> elements = FromCMCardToCardDetail.newInstance() //
					.withDataView(userDataView()) //
					.withErrorHandler(errorHandler()) //
					.build() //
					.apply(fetched);
			return CardResponse.newInstance() //
					.withElement(elements) //
					.build();
		} catch (final NotFoundException e) {
			errorHandler().cardNotFound(id);
			throw new AssertException("should not come here");
		}

	}

}
