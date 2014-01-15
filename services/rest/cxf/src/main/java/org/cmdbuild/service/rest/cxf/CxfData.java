package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.service.rest.Data;
import org.cmdbuild.service.rest.dto.data.AttributeDetail;
import org.cmdbuild.service.rest.dto.data.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.data.CardDetail;
import org.cmdbuild.service.rest.dto.data.CardDetailResponse;
import org.cmdbuild.service.rest.serialization.data.ToAttributeDetail;
import org.cmdbuild.service.rest.serialization.data.ToCardDetail;

import com.mchange.util.AssertException;

public class CxfData extends CxfService implements Data {

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
	public AttributeDetailResponse getAttributes(final String name, final Long id) {
		// TODO inject error management within logic
		if (userDataView().findClass(name) == null) {
			errorHandler().classNotFound(name);
		}
		try {
			final CMCard fetched = userDataAccessLogic().fetchCMCard(name, id);

			final ToAttributeDetail toAttributeDetail = ToAttributeDetail.newInstance() //
					.with(fetched.getType()) //
					.build();
			final Iterable<AttributeDetail> details = from(fetched.getAllValues()) //
					.transform(toAttributeDetail);
			return AttributeDetailResponse.newInstance() //
					.withDetails(details) //
					.withTotal(size(details)) //
					.build();
		} catch (final NotFoundException e) {
			errorHandler().cardNotFound(id);
			throw new AssertException("should not come here");
		}

	}

}
