package org.cmdbuild.report;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class ReportFactoryTemplateSchema extends ReportFactoryTemplate {

	private JasperDesign jasperDesign;
	private ReportExtension reportExtension;
	private final static String REPORT = "CMDBuild_dbschema.jrxml";

	public ReportFactoryTemplateSchema(final ReportExtension reportExtension) throws JRException {
		this(reportExtension, null);
	}

	public ReportFactoryTemplateSchema(final ReportExtension reportExtension, final String className) throws JRException {
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