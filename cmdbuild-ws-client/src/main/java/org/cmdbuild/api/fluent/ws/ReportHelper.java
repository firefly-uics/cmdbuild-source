package org.cmdbuild.api.fluent.ws;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Report;
import org.cmdbuild.services.soap.ReportParams;

class ReportHelper extends WsHelper {

	public static final String DEFAULT_TYPE = "custom";
	public static final String TEMPORARY_FILE_PREFIX = "report";

	private static final ReportParams EMPTY_PARAMS = new ReportParams();

	public ReportHelper(final Private proxy) {
		super(proxy);
	}

	public List<Report> getReports(final String type) {
		return proxy().getReportList(type, Integer.MAX_VALUE, 0);
	}

	public Report getReport(final String type, final String title) {
		final List<Report> reports = getReports(type);
		for (final Report report : reports) {
			if (report.getTitle().equals(title)) {
				return report;
			}
		}
		final String message = format("missing report for type '%s' and title '%s'", type, title);
		throw new IllegalArgumentException(message);
	}

	public List<ReportParams> compileParams(final Map<String, Object> params) {
		final List<ReportParams> reportParameters = new ArrayList<ReportParams>();
		for (final Entry<String, Object> entry : params.entrySet()) {
			final ReportParams parameter = new ReportParams();
			parameter.setKey(entry.getKey());
			parameter.setValue(convertToWsType(entry.getValue()));
			reportParameters.add(parameter);
		}
		if (reportParameters.isEmpty()) {
			reportParameters.add(EMPTY_PARAMS);
		}
		return reportParameters;
	}

	public DataHandler getDataHandler(final Report report, final String format, final List<ReportParams> reportParams) {
		final int id = report.getId();
		return proxy().getReport(id, format, reportParams);
	}

	public File temporaryFile() {
		try {
			final File file = File.createTempFile(TEMPORARY_FILE_PREFIX, null);
			file.deleteOnExit();
			return file;
		} catch (final IOException e) {
			final String message = "error creating temporary file";
			throw new IllegalArgumentException(message);
		}
	}

	public void saveToFile(final DataHandler dataHandler, final File file) {
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			dataHandler.writeTo(outputStream);
		} catch (final IOException e) {
			final String message = format("error saving report to file '%s'", file.getAbsolutePath());
			throw new IllegalArgumentException(message);
		} finally {
			try {
				outputStream.close();
			} catch (final IOException e) {
				final String message = format("error closing stream");
				throw new IllegalArgumentException(message);
			}
		}
	}

}
