package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.service.rest.v2.ClassAttributes;
import org.cmdbuild.service.rest.v2.cxf.serialization.AttributeTypeResolver;
import org.cmdbuild.service.rest.v2.cxf.serialization.ToAttributeDetail;
import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.services.meta.MetadataStoreFactory;

public class CxfClassAttributes implements ClassAttributes {

	private static final AttributeTypeResolver ATTRIBUTE_TYPE_RESOLVER = new AttributeTypeResolver();

	private final ErrorHandler errorHandler;
	private final DataAccessLogic userDataAccessLogic;
	private final CMDataView systemDataView;
	private final MetadataStoreFactory metadataStoreFactory;
	private final LookupLogic lookupLogic;

	public CxfClassAttributes(final ErrorHandler errorHandler, final DataAccessLogic userDataAccessLogic,
			final CMDataView systemDataView, final MetadataStoreFactory metadataStoreFactory,
			final LookupLogic lookupLogic) {
		this.errorHandler = errorHandler;
		this.userDataAccessLogic = userDataAccessLogic;
		this.systemDataView = systemDataView;
		this.metadataStoreFactory = metadataStoreFactory;
		this.lookupLogic = lookupLogic;
	}

	@Override
	public ResponseMultiple<Attribute> readAll(final String classId, final boolean activeOnly, final Integer limit,
			final Integer offset) {
		final CMClass target = userDataAccessLogic.findClass(classId);
		if (target == null) {
			errorHandler.classNotFound(classId);
		}
		if (userDataAccessLogic.isProcess(target)) {
			errorHandler.classNotFoundClassIsProcess(classId);
		}
		final PagedElements<CMAttribute> filteredAttributes = userDataAccessLogic.getAttributes( //
				target.getName(), //
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
				.withLookupLogic(lookupLogic) //
				.build();
		final Iterable<Attribute> elements = from(filteredAttributes) //
				.transform(toAttributeDetails);
		return newResponseMultiple(Attribute.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(filteredAttributes.totalSize())) //
						.build()) //
				.build();
	}

}
