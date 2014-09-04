package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.service.rest.ProcessAttributes;
import org.cmdbuild.service.rest.cxf.serialization.AttributeTypeResolver;
import org.cmdbuild.service.rest.cxf.serialization.ToAttributeDetail;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.services.meta.MetadataStoreFactory;

public class CxfProcessAttributes implements ProcessAttributes {

	private static final AttributeTypeResolver ATTRIBUTE_TYPE_RESOLVER = new AttributeTypeResolver();

	private final ErrorHandler errorHandler;
	private final DataAccessLogic userDataAccessLogic;
	private final CMDataView systemDataView;
	private final MetadataStoreFactory metadataStoreFactory;

	public CxfProcessAttributes(final ErrorHandler errorHandler, final DataAccessLogic userDataAccessLogic,
			final CMDataView systemDataView, final MetadataStoreFactory metadataStoreFactory) {
		this.errorHandler = errorHandler;
		this.userDataAccessLogic = userDataAccessLogic;
		this.systemDataView = systemDataView;
		this.metadataStoreFactory = metadataStoreFactory;
	}

	@Override
	public ListResponse<AttributeDetail> readAll(final String name, final boolean activeOnly, final Integer limit,
			final Integer offset) {
		final CMClass target = userDataAccessLogic.findClass(name);
		if (target == null) {
			errorHandler.typeNotFound(name);
		}
		final PagedElements<CMAttribute> filteredAttributes = userDataAccessLogic.getAttributes( //
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
				.withDataView(systemDataView) //
				.withErrorHandler(errorHandler) //
				.withMetadataStoreFactory(metadataStoreFactory) //
				.build();
		final Iterable<AttributeDetail> elements = from(filteredAttributes) //
				.transform(toAttributeDetails);
		return ListResponse.<AttributeDetail> newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(Long.valueOf(filteredAttributes.totalSize())) //
						.build()) //
				.build();
	}

}
