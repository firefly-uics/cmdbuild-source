package org.cmdbuild.servlets.resource.shark;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import org.cmdbuild.elements.report.ReportFactoryDB;
import org.cmdbuild.elements.report.ReportParameter;
import org.cmdbuild.elements.report.ReportFactory.ReportExtension;
import org.cmdbuild.elements.report.ReportFactory.ReportType;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.exception.ReportException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.servlets.resource.RESTExported;
import org.cmdbuild.servlets.resource.RESTExported.RestMethod;
import org.cmdbuild.servlets.utils.Parameter;

public class CreateReportToolAgentExecutor extends AbstractSharkResource {

	public String baseURI() {
		return "createreport";
	}
	
	@RESTExported(
	httpMethod = RestMethod.POST
	)
	public String createReportAndGetURL(
		@Parameter("code") String code,
		@Parameter("type") String type,
		@Parameter("format") String format,
		@Parameter("cmdburl") String url,
		Map<String,String> params
	) throws Exception
	{
		Log.WORKFLOW.debug("creating report " + type + ": " + code + "." + format);
		
		ReportType rtType = ReportType.valueOf(type.toUpperCase());
		
		for(String key : params.keySet()) {
			Log.WORKFLOW.debug(key + " => " + params.get(key));
		}
		url = (url.endsWith("/") ? url : url + "/");
		String out = null;
		ReportCard wrap = null;
		ReportFactoryDB factory = null;
		for(ReportCard report : ReportCard.findReportsByType(rtType)) {
			if(report.getCode().equalsIgnoreCase(code)) {
				wrap = report;
				break;
			}
		}
		
		if(wrap == null) {
			throw ReportException.ReportExceptionType.REPORT_NOTFOUND.createException(type,code);
		}
				
		if(rtType == ReportType.CUSTOM) {
			ReportExtension reportExtension = ReportExtension.valueOf(format.toUpperCase());
			factory = new ReportFactoryDB(wrap.getId(),reportExtension);
			if(factory.getReportParameters().isEmpty()) {
				Log.REPORT.debug("report is filled");
				factory.fillReport();
				out = produceReport(factory);
			} else {
				for(ReportParameter reportParameter : factory.getReportParameters()) {
					// update parameter
					reportParameter.parseValue(params.get(reportParameter.getFullName()));
					Log.REPORT.debug("Setting parameter "+reportParameter.getFullName()+": "+reportParameter.getValue());
				}
				factory.fillReport();
				out = produceReport(factory);
			}
		}
		return url + out;
	}

	private String getNormalizedCode(ReportFactoryDB factory) {
		return factory.getReportCard().getCode().replace(' ', '_');
	}
	private String produceReport(ReportFactoryDB factory) throws Exception {
		File file = File.createTempFile(getNormalizedCode(factory), "."+ factory.getReportExtension());
		String fileName = file.getName();
		Log.WORKFLOW.debug("created file: " + fileName + ", fullpath: " + file.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(file);
		
		factory.sendReportToStream(fos);
		fos.flush(); fos.close();
		return fileName;
	}
}
