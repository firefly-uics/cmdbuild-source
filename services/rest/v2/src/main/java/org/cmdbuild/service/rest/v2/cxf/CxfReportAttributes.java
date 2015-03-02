package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.report.ReportLogic;
import org.cmdbuild.logic.report.ReportLogic.Report;
import org.cmdbuild.service.rest.v2.ReportAttributes;
import org.cmdbuild.service.rest.v2.cxf.serialization.AttributeTypeResolver;
import org.cmdbuild.service.rest.v2.cxf.serialization.ToAttributeDetail;
import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;

import com.google.common.base.Optional;

public class CxfReportAttributes implements ReportAttributes {

	private static final AttributeTypeResolver ATTRIBUTE_TYPE_RESOLVER = new AttributeTypeResolver();

	private final ReportLogic reportLogic;
	private final ErrorHandler errorHandler;
	private final ToAttributeDetail toAttributeDetail;

	public CxfReportAttributes(final ErrorHandler errorHandler, final ReportLogic reportLogic,
			final CMDataView dataView, final LookupLogic lookupLogic) {
		this.reportLogic = reportLogic;
		this.errorHandler = errorHandler;
		this.toAttributeDetail = ToAttributeDetail.newInstance() //
				.withAttributeTypeResolver(ATTRIBUTE_TYPE_RESOLVER) //
				.withDataView(dataView) //
				.withErrorHandler(errorHandler) //
				.withLookupLogic(lookupLogic) //
				.build();
	}

	@Override
	public ResponseMultiple<Attribute> readAll(final Long reportId, final Integer limit, final Integer offset) {
		final Optional<Report> report = reportLogic.read(reportId.intValue());
		if (!report.isPresent()) {
			errorHandler.reportNotFound(reportId);
		}
		final Iterable<CMAttribute> elements = reportLogic.parameters(reportId.intValue());
		return newResponseMultiple(Attribute.class) //
				.withElements(from(elements) //
						.skip(defaultIfNull(offset, 0)) //
						.limit(defaultIfNull(limit, MAX_VALUE)) //
						.transform(toAttributeDetail)) //
				.withMetadata(newMetadata() //
						.withTotal(size(elements)) //
						.build()) //
				.build();
	}

}
