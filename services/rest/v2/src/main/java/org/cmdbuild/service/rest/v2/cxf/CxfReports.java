package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.v2.model.Models.newLongIdAndDescription;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;

import org.cmdbuild.logic.report.ReportLogic;
import org.cmdbuild.logic.report.ReportLogic.Report;
import org.cmdbuild.service.rest.v2.Reports;
import org.cmdbuild.service.rest.v2.model.LongIdAndDescription;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;

import com.google.common.base.Function;

public class CxfReports implements Reports {

	private final ReportLogic logic;

	public CxfReports(final ReportLogic logic) {
		this.logic = logic;
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
}
