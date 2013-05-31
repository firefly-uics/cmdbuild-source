package org.cmdbuild.report;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.config.CmdbuildConfiguration;

public class ReportFactoryTemplateSchema extends ReportFactoryTemplate {

	private final JasperDesign jasperDesign;
	private final ReportExtension reportExtension;
	private final static String REPORT = "CMDBuild_dbschema.jrxml";

	public ReportFactoryTemplateSchema( //
			final DataSource dataSource, //
			final ReportExtension reportExtension, //
			final CmdbuildConfiguration configuration //
	) throws JRException {
		this(dataSource, reportExtension, null, configuration);
	}

	public ReportFactoryTemplateSchema( //
			final DataSource dataSource, //
			final ReportExtension reportExtension, //
			final String className, //
			final CmdbuildConfiguration configuration //
	) throws JRException {
		super(dataSource, configuration);
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
