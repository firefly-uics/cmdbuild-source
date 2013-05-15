package org.cmdbuild.services.soap.operation;

import static java.lang.String.format;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.services.auth.UserType;

public enum BuiltInReport {

	LIST("_list") {
		@Override
		public ReportFactoryBuilder<ReportFactory> newBuilder(final CMDataView dataView, final UserType userType) {
			return new ListReportFactoryBuilder(dataView, userType);
		}
	},
	;

	private final String reportId;

	private BuiltInReport(final String reportId) {
		this.reportId = reportId;
	}

	public static BuiltInReport from(final String reportId) {
		for (final BuiltInReport report : values()) {
			if (report.reportId.equals(reportId)) {
				return report;
			}
		}
		throw new Error(format("undefined report '%s'", reportId));
	}

	public abstract ReportFactoryBuilder<ReportFactory> newBuilder(CMDataView dataView, UserType userType);

}
