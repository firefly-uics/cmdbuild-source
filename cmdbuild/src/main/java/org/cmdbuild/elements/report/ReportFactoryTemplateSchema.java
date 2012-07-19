package org.cmdbuild.elements.report;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.elements.interfaces.ITable;

public class ReportFactoryTemplateSchema extends ReportFactoryTemplate {
	
	private JasperDesign jasperDesign;
	private ReportExtension reportExtension;
	private final static String REPORT = "CMDBuild_dbschema.jrxml";

	public ReportFactoryTemplateSchema(ReportExtension reportExtension) throws JRException {
		this.reportExtension = reportExtension;
		jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT);
		updateImagesPath();
		updateSubreportsPath();
	}
	
	public ReportFactoryTemplateSchema(ReportExtension reportExtension, ITable iTable) throws JRException {
		this.reportExtension = reportExtension;
		jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT);
		jasperDesign.setName(iTable.getName());
		addFillParameter("class", iTable.getDBName());
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