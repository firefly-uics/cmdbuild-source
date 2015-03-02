package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.v2.model.Models.newLongIdAndDescription;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newValues;

import javax.activation.DataHandler;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.report.ReportLogic;
import org.cmdbuild.logic.report.ReportLogic.Report;
import org.cmdbuild.service.rest.v2.Reports;
import org.cmdbuild.service.rest.v2.cxf.serialization.AttributeTypeResolver;
import org.cmdbuild.service.rest.v2.cxf.serialization.ToAttributeDetail;
import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.JsonValues;
import org.cmdbuild.service.rest.v2.model.LongIdAndDescription;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.Values;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class CxfReports implements Reports {

	private static final AttributeTypeResolver ATTRIBUTE_TYPE_RESOLVER = new AttributeTypeResolver();

	private static final Values NO_PARAMETERS = newValues().build();

	private final ReportLogic logic;
	private final ErrorHandler errorHandler;
	private final ToAttributeDetail toAttributeDetail;

	public CxfReports(final ErrorHandler errorHandler, final ReportLogic logic, final CMDataView dataView,
			final LookupLogic lookupLogic) {
		this.logic = logic;
		this.errorHandler = errorHandler;
		this.toAttributeDetail = ToAttributeDetail.newInstance() //
				.withAttributeTypeResolver(ATTRIBUTE_TYPE_RESOLVER) //
				.withDataView(dataView) //
				.withErrorHandler(errorHandler) //
				.withLookupLogic(lookupLogic) //
				.build();
	}

	@Override
	public ResponseMultiple<LongIdAndDescription> readAll(final Integer limit, final Integer offset) {
		final Iterable<Report> elements = logic.readAll();
		return newResponseMultiple(LongIdAndDescription.class) //
				.withElements(from(elements) //
						.transform(new Function<Report, LongIdAndDescription>() {

							@Override
							public LongIdAndDescription apply(final Report input) {
								return newLongIdAndDescription() //
										.setId(Long.valueOf(input.getId())) //
										.setDescription(input.getDescription()) //
										.build();
							}

						}) //
						.skip(defaultIfNull(offset, 0)) //
						.limit(defaultIfNull(limit, MAX_VALUE)) //
				) //
				.withMetadata(newMetadata() //
						.withTotal(size(elements)) //
						.build()) //
				.build();
	}

	@Override
	public ResponseMultiple<Attribute> readAllAttributes(final Long reportId, final Integer limit, final Integer offset) {
		final Optional<Report> report = logic.read(reportId.intValue());
		if (!report.isPresent()) {
			errorHandler.reportNotFound(reportId);
		}
		final Iterable<CMAttribute> elements = logic.parameters(reportId.intValue());
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

	@Override
	public DataHandler download(final Long reportId, final String extension, final JsonValues parameters) {
		final Optional<Report> report = logic.read(reportId.intValue());
		if (!report.isPresent()) {
			errorHandler.reportNotFound(reportId);
		}
		// TODO check extension
		return logic.download(reportId.intValue(), extension, defaultIfNull(parameters, NO_PARAMETERS));
	}

}
