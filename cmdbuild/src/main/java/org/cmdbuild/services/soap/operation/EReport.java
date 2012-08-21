package org.cmdbuild.services.soap.operation;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.report.ReportFactoryDB;
import org.cmdbuild.elements.report.ReportParameter;
import org.cmdbuild.elements.report.ReportFactory.ReportExtension;
import org.cmdbuild.elements.report.ReportFactory.ReportType;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.types.Report;
import org.cmdbuild.services.soap.types.ReportParams;

public class EReport {
	
	private UserContext userCtx;

	public EReport(UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public Report[] getReportList(String type, int limit, int offset) {
		ITableFactory tf = userCtx.tables();
		List<Report> reportList = new ArrayList<Report>();
		int numRecords = 0;
		ReportType reportType = ReportType.valueOf(type.toUpperCase());
		for (ReportCard report : ReportCard.findReportsByType(reportType)) {
			if (report.isUserAllowed(userCtx)) {
				++numRecords;
				if (limit > 0 && numRecords > offset && numRecords <= offset + limit) {
					reportList.add(serializeReport(report, tf));
				} 
			}
		}
		return reportList.toArray(new Report[reportList.size()]);
	}
	
	public AttributeSchema[] getReportParameters(int id, String extension)  {
		ITableFactory tf = userCtx.tables();
		ReportFactoryDB reportFactory;
		try {
			reportFactory = new ReportFactoryDB(id, ReportExtension.valueOf(extension.toUpperCase()));
			EAdministration administration = new EAdministration(userCtx);
			List<AttributeSchema> reportParameterList = new ArrayList<AttributeSchema>();
			for (ReportParameter reportParameter : reportFactory.getReportParameters()) {
				IAttribute reportAttribute = reportParameter.createCMDBuildAttribute(tf);
				AttributeSchema attribute = administration.serialize(reportAttribute);
				reportParameterList.add(attribute);
			}
			return reportParameterList.toArray(new AttributeSchema[reportParameterList.size()]);
		} catch (SQLException e) {
			Log.SOAP.error("SQL error in report");
			Log.SOAP.debug(e);
		} catch (IOException e) {
			Log.SOAP.error("Error reading report");
			Log.SOAP.debug(e);
		} catch (ClassNotFoundException e) {
			Log.SOAP.error("Cannot find class in report");
			Log.SOAP.debug(e);
		}
		return null;
	}
	
	public DataHandler getReport(int id, String extension, ReportParams[] params)  {
		ReportExtension reportExtension = ReportExtension.valueOf(extension.toUpperCase());
		try {
			ReportFactoryDB reportFactory = new ReportFactoryDB(id, reportExtension);
			if (params != null){
				for(ReportParameter reportParameter : reportFactory.getReportParameters()) {
					for (ReportParams param : params){
						if (param.getKey().equals(reportParameter.getName())) {
							// update parameter
							reportParameter.parseValue(param.getValue());
						}
					}
				}
			}
			reportFactory.fillReport();
			String filename = reportFactory.getReportCard().getCode().replaceAll(" " , "");
			// add extension
			filename += "." + reportFactory.getReportExtension().toString().toLowerCase();
			// send to stream
			DataSource dataSource = TempDataSource.create(filename, reportFactory.getContentType());
			OutputStream outputStream = dataSource.getOutputStream();
			reportFactory.sendReportToStream(outputStream);
			return new DataHandler(dataSource);
		} catch (SQLException e) {
			Log.SOAP.error("SQL error in report");
			Log.SOAP.debug(e);
		} catch (IOException e) {
			Log.SOAP.error("Error reading report");
			Log.SOAP.debug(e);
		} catch (ClassNotFoundException e) {
			Log.SOAP.error("Cannot find class in report");
			Log.SOAP.debug(e);
		} catch (Exception e) {
			Log.SOAP.error("Error getting report", e);
		}
		return null;
	}

	private Report serializeReport(ReportCard reportCard, ITableFactory tf) {
		Report report = new Report();
		report.setDescription(reportCard.getDescription());
		report.setId(reportCard.getId());
		report.setTitle(reportCard.getCode());
		report.setType(reportCard.getType().toString());
		return report;
	}

}
