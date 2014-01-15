package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.service.rest.Data;
import org.cmdbuild.service.rest.dto.CardDetail;
import org.cmdbuild.service.rest.dto.CardDetailResponse;
import org.cmdbuild.service.rest.serialization.ToCardDetail;

public class CxfData extends CxfService implements Data {

	@Override
	public CardDetailResponse getCards(final String name) {
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(Integer.MAX_VALUE) //
				.offset(0) //
				.build();
		final FetchCardListResponse response = dataAccessLogic().fetchCards(name, queryOptions);

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

}
