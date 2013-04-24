package org.cmdbuild.report;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class ReportFactoryTemplateSchema extends ReportFactoryTemplate {

	private JasperDesign jasperDesign;
	private ReportExtension reportExtension;
	private final static String REPORT = "CMDBuild_dbschema.jrxml";

	public ReportFactoryTemplateSchema(
			final DataSource dataSource, //
			final ReportExtension reportExtension) throws JRException {
		this(dataSource, reportExtension, null);
	}

	public ReportFactoryTemplateSchema(
			final DataSource dataSource, //
			final ReportExtension reportExtension, final String className) throws JRException {
		super(dataSource);
		this.reportExtension = reportExtension;
		jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT);
		if (className != null) {
			jasperDesign.setName(className);
			addFillParameter("class", className);
		}
		updateImagesPath();
		updateSubreportsPath();
	}

	@Override
	public JasperDesign getJasperDesign() {
		return jasperDesign;
	}

	@Override
	public ReportExtension getReportExtension() {
		return reportExtension;
	}
}