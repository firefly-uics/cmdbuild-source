package org.cmdbuild.servlets.resource.shark;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import org.cmdbuild.elements.report.ReportFactory.ReportExtension;
import org.cmdbuild.elements.report.ReportFactory.ReportType;
import org.cmdbuild.elements.report.ReportFactoryDB;
import org.cmdbuild.elements.report.ReportParameter;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.exception.ReportException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.servlets.resource.RESTExported;
import org.cmdbuild.servlets.resource.RESTExported.RestMethod;
import org.cmdbuild.servlets.utils.Parameter;

public class CreateReportToolAgentExecutor extends AbstractSharkResource {

	@Override
	public String baseURI() {
		return "createreport";
	}

	@RESTExported(httpMethod = RestMethod.POST)
	public String createReportAndGetURL(@Parameter("code") final String code, @Parameter("type") final String type,
			@Parameter("format") final String format, @Parameter("cmdburl") String url, final Map<String, String> params)
			throws Exception {
		Log.WORKFLOW.debug("creating report " + type + ": " + code + "." + format);

		final ReportType rtType = ReportType.valueOf(type.toUpperCase());

		for (final String key : params.keySet()) {
			Log.WORKFLOW.debug(key + " => " + params.get(key));
		}
		url = (url.endsWith("/") ? url : url + "/");
		String out = null;
		final ReportCard wrap = ReportCard.findReportByTypeAndCode(rtType, code);

		if (wrap == null) {
			throw ReportException.ReportExceptionType.REPORT_NOTFOUND.createException(type, code);
		}

		if (rtType == ReportType.CUSTOM) {
			final ReportExtension reportExtension = ReportExtension.valueOf(format.toUpperCase());
			final ReportFactoryDB factory = new ReportFactoryDB(wrap.getId(), reportExtension);
			if (factory.getReportParameters().isEmpty()) {
				Log.REPORT.debug("report is filled");
				factory.fillReport();
				out = produceReport(factory);
			} else {
				for (final ReportParameter reportParameter : factory.getReportParameters()) {
					// update parameter
					reportParameter.parseValue(params.get(reportParameter.getFullName()));
					Log.REPORT.debug("Setting parameter " + reportParameter.getFullName() + ": "
							+ reportParameter.getValue());
				}
				factory.fillReport();
				out = produceReport(factory);
			}
		}
		return url + out;
	}

	private String getNormalizedCode(final ReportFactoryDB factory) {
		return factory.getReportCard().getCode().replace(' ', '_');
	}

	private String produceReport(final ReportFactoryDB factory) throws Exception {
		final File file = File.createTempFile(getNormalizedCode(factory), "." + factory.getReportExtension());
		final String fileName = file.getName();
		Log.WORKFLOW.debug("created file: " + fileName + ", fullpath: " + file.getAbsolutePath());
		final FileOutputStream fos = new FileOutputStream(file);

		factory.sendReportToStream(fos);
		fos.flush();
		fos.close();
		return fileName;
	}
}
