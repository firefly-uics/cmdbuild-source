package org.cmdbuild.services.soap.operation;

import static java.lang.String.format;
import static org.cmdbuild.services.soap.operation.SerializationStuff.serialize;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.Report;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.report.ReportFactoryDB;
import org.cmdbuild.report.ReportParameter;
import org.cmdbuild.report.ReportFactory.ReportExtension;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.types.ReportParams;
import org.cmdbuild.services.store.report.JDBCReportStore;
import org.cmdbuild.services.store.report.ReportStore;

import com.google.common.collect.Maps;

public class EReport {

	private final UserContext userCtx;
	private final ReportStore reportStore;

	public EReport(final UserContext userCtx) {
		this.userCtx = userCtx;
		this.reportStore = applicationContext().getBean(JDBCReportStore.class);
	}

	public org.cmdbuild.services.soap.types.Report[] getReportList(final String type, final int limit, final int offset) {
		final ITableFactory tf = UserOperations.from(userCtx).tables();
		final List<org.cmdbuild.services.soap.types.Report> reportList = new ArrayList<org.cmdbuild.services.soap.types.Report>();
		int numRecords = 0;
		final ReportType reportType = ReportType.valueOf(type.toUpperCase());
		for (final Report report : reportStore.findReportsByType(reportType)) {
			if (report.isUserAllowed()) {
				++numRecords;
				if (limit > 0 && numRecords > offset && numRecords <= offset + limit) {
					reportList.add(serializeReport(report, tf));
				}
			}
		}

		return reportList.toArray(new org.cmdbuild.services.soap.types.Report[reportList.size()]);
	}

	public AttributeSchema[] getReportParameters(final int id, final String extension) {
		final ITableFactory tf = UserOperations.from(userCtx).tables();
		ReportFactoryDB reportFactory;
		try {
			reportFactory = new ReportFactoryDB(reportStore, id, ReportExtension.valueOf(extension.toUpperCase()));
			final EAdministration administration = new EAdministration(userCtx);
			final List<AttributeSchema> reportParameterList = new ArrayList<AttributeSchema>();
			for (final ReportParameter reportParameter : reportFactory.getReportParameters()) {
				final CMAttribute reportAttribute = reportParameter.createCMDBuildAttribute();
				final AttributeSchema attribute = serialize(reportAttribute);
				reportParameterList.add(attribute);
			}
			return reportParameterList.toArray(new AttributeSchema[reportParameterList.size()]);
		} catch (final SQLException e) {
			Log.SOAP.error("SQL error in report", e);
		} catch (final IOException e) {
			Log.SOAP.error("Error reading report", e);
		} catch (final ClassNotFoundException e) {
			Log.SOAP.error("Cannot find class in report", e);
		}
		return null;
	}

	public DataHandler getReport(final int id, final String extension, final ReportParams[] params) {
		final ReportExtension reportExtension = ReportExtension.valueOf(extension.toUpperCase());
		try {
			final ReportFactoryDB reportFactory = new ReportFactoryDB(reportStore, id, reportExtension);
			if (params != null) {
				for (final ReportParameter reportParameter : reportFactory.getReportParameters()) {
					for (final ReportParams param : params) {
						if (param.getKey().equals(reportParameter.getName())) {
							// update parameter
							reportParameter.parseValue(param.getValue());
						}
					}
				}
			}
			reportFactory.fillReport();
			String filename = reportFactory.getReportCard().getCode().replaceAll(" ", "");
			// add extension
			filename += "." + reportFactory.getReportExtension().toString().toLowerCase();
			// send to stream
			final DataSource dataSource = TempDataSource.create(filename, reportFactory.getContentType());
			final OutputStream outputStream = dataSource.getOutputStream();
			reportFactory.sendReportToStream(outputStream);
			return new DataHandler(dataSource);
		} catch (final SQLException e) {
			Log.SOAP.error("SQL error in report", e);
		} catch (final IOException e) {
			Log.SOAP.error("Error reading report", e);
		} catch (final ClassNotFoundException e) {
			Log.SOAP.error("Cannot find class in report", e);
		} catch (final Exception e) {
			Log.SOAP.error("Error getting report", e);
		}
		return null;
	}

	private org.cmdbuild.services.soap.types.Report serializeReport(final Report reportCard, final ITableFactory tf) {
		final org.cmdbuild.services.soap.types.Report report = new org.cmdbuild.services.soap.types.Report();
		report.setDescription(reportCard.getDescription());
		report.setId(reportCard.getId());
		report.setTitle(reportCard.getCode());
		report.setType(reportCard.getType().toString());
		return report;
	}

	private static enum BuiltInReport {

		LIST("_list") {
			@Override
			public ReportFactoryBuilder<ReportFactory> newBuilder(final UserContext userContext) {
				return new ListReportFactoryBuilder(userContext);
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

		public abstract ReportFactoryBuilder<ReportFactory> newBuilder(UserContext userContext);

	}

	public DataHandler getReport(final String reportId, final String extension, final ReportParams[] params) {
		try {
			final BuiltInReport builtInReport = BuiltInReport.from(reportId);
			final ReportFactory reportFactory = builtInReport.newBuilder(userCtx) //
					.withExtension(extension) //
					.withProperties(propertiesFrom(params)) //
					.build();
			reportFactory.fillReport();
			final DataSource dataSource = TempDataSource.create(null, reportFactory.getContentType());
			final OutputStream outputStream = dataSource.getOutputStream();
			reportFactory.sendReportToStream(outputStream);
			return new DataHandler(dataSource);
		} catch (final Throwable e) {
			throw new Error(e);
		}
	}

	private Map<String, String> propertiesFrom(final ReportParams[] params) {
		final Map<String, String> properties = Maps.newHashMap();
		for (final ReportParams param : params) {
			properties.put(param.getKey(), param.getValue());
		}
		return properties;
	}

}
