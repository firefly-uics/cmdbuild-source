package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.service.rest.ClassAttributes;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.AttributeDetailResponse;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.serialization.AttributeTypeResolver;
import org.cmdbuild.service.rest.serialization.ToAttributeDetail;

public class CxfClassAttributes extends CxfService implements ClassAttributes {

	private static final AttributeTypeResolver ATTRIBUTE_TYPE_RESOLVER = new AttributeTypeResolver();

	@Context
	protected SecurityContext securityContext;

	@Context
	protected UriInfo uriInfo;

	@Override
	public AttributeDetailResponse getAttributes(final String name, final boolean activeOnly, final Integer limit,
			final Integer offset) {
		final CMClass target = userDataAccessLogic().findClass(name);
		if (target == null) {
			errorHandler().entryTypeNotFound(name);
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
				.withMetadataStoreFactory(metadataStoreFactory()) //
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

}
