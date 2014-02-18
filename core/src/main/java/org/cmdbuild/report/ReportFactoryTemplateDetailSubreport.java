package org.cmdbuild.report;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.report.query.RelationsReportQuery;
import org.cmdbuild.services.localization.Localization;

public class ReportFactoryTemplateDetailSubreport extends ReportFactoryTemplate {

	public static enum SubreportType {
		RELATIONS
	}

	private static class SubreportAttribute {

		private final String queryName;
		private final String label;
		private final CMAttribute cmAttribute;

		public SubreportAttribute(final String queryName, final String label, final CMAttribute attribute) {
			super();
			this.queryName = queryName;
			this.label = label;
			cmAttribute = attribute;
		}

		public String getQueryName() {
			return queryName;
		}

		public String getLabel() {
			return label;
		}

		public CMAttribute getCMDBuildAttribute() {
			return cmAttribute;
		}
	}

	private final static String REPORT = "CMDBuild_card_detail_subreport.jrxml";

	private final JasperDesign jasperDesign;
	private final ReportExtension reportExtension;
	private final List<SubreportAttribute> attributes;
	private String designTitle;
	private final Localization localization;

	@Override
	public JasperDesign getJasperDesign() {
		return jasperDesign;
	}

	@Override
	public ReportExtension getReportExtension() {
		return reportExtension;
	}

	public ReportFactoryTemplateDetailSubreport(final DataSource dataSource, final SubreportType subreportType,
			final CMClass table, final Card card, final CMDataView dataView, final Localization localization,
			final CmdbuildConfiguration configuration) throws JRException {
		super(dataSource, configuration, dataView);
		// init vars
		this.reportExtension = ReportExtension.PDF;
		this.attributes = new LinkedList<SubreportAttribute>();
		this.localization = localization;

		String query = "";

		switch (subreportType) {

		case RELATIONS:

			designTitle = getTranslation("management.modcard.tabs.relations");
			attributes.add( //
					new SubreportAttribute( //
							RelationsReportQuery.DOMAIN_DESCRIPTION, //
							getTranslation("management.modcard.relation_columns.domain"), //
							ReportParameterConverter.of(new RPFake(RelationsReportQuery.DESCRIPTION)).toCMAttribute()));
			attributes.add( //
					new SubreportAttribute( //
							RelationsReportQuery.CLASS_DESCRIPTION, //
							getTranslation("management.modcard.relation_columns.destclass"), //
							ReportParameterConverter.of(new RPFake(RelationsReportQuery.DESCRIPTION)).toCMAttribute()));
			attributes.add( //
					new SubreportAttribute( //
							"begindate", //
							getTranslation("management.modcard.relation_columns.begin_date"), //
							ReportParameterConverter.of(new RPFake(RelationsReportQuery.BEGIN_DATE)).toCMAttribute()));
			attributes.add( //
					new SubreportAttribute( //
							RelationsReportQuery.CODE, //
							getTranslation("management.modcard.relation_columns.code"), //
							ReportParameterConverter.of(new RPFake(RelationsReportQuery.CODE)).toCMAttribute()));
			attributes.add( //
					new SubreportAttribute( //
							RelationsReportQuery.DESCRIPTION, //
							getTranslation("management.modcard.relation_columns.description"), //
							ReportParameterConverter.of(new RPFake(RelationsReportQuery.DESCRIPTION)).toCMAttribute()));

			final Iterable<? extends CMDomain> domains = dataView.findDomainsFor(table);
			query = new RelationsReportQuery(card, domains).toString();

			break;
		}

		// load design
		jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT);

		// initialize design
		Log.REPORT.debug(String.format("Report on relations query: %s", query));
		initDesign(query);
	}

	private String getTranslation(final String key) {
		return localization.get(key);
	}

	public JasperReport compileReport() throws JRException {
		JasperReport subreport = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		ByteArrayInputStream byteArrayInputStream = null;
		try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			JasperCompileManager.compileReportToStream(jasperDesign, byteArrayOutputStream);
			byteArrayOutputStream.flush();
			byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			subreport = (JasperReport) JRLoader.loadObject(byteArrayInputStream);
		} catch (final IOException e) {
			try {
				if (byteArrayOutputStream != null) {
					byteArrayOutputStream.close();
				}
			} catch (final IOException e1) {
				// do nothing
			}
		}

		return subreport;
	}

	private void initDesign(final String query) throws JRException {
		// set report query
		setQuery(query);

		// set report fields
		deleteAllFields();
		for (final SubreportAttribute attribute : attributes) {
			final CMAttribute cmAttribute = attribute.getCMDBuildAttribute();
			if (cmAttribute == null) {
				continue;
			}

			String name = "";
			if (attribute.getQueryName() != null) {
				name = attribute.getQueryName();
			} else {
				name = attribute.getCMDBuildAttribute().getName();
			}

			addField(name, attribute.getCMDBuildAttribute().getDescription(), attribute.getCMDBuildAttribute()
					.getType());
		}

		// set column header
		setColumnHeader();

		// set detail
		setDetail();

		// set design parameters
		addDesignParameter("title", designTitle);
	}

	private void setColumnHeader() {
		final JRBand band = jasperDesign.getColumnHeader();

		// clear band
		band.getChildren().clear();

		// add texts
		final int horizontalStep = Math.round(jasperDesign.getPageWidth() / attributes.size());
		int x = 0;
		final int y = 4;
		for (final SubreportAttribute attribute : attributes) {
			final JRDesignStaticText st = createStaticText(attribute.getLabel());
			st.setMode(ModeEnum.OPAQUE);
			st.setBackcolor(new Color(236, 236, 236)); // gray
			st.setHeight(20);
			st.setWidth(horizontalStep);
			st.setX(x);
			st.setY(y);
			band.getChildren().add(st);

			x += horizontalStep;
		}
	}

	private void setDetail() {
		final JRSection section = jasperDesign.getDetailSection();
		final JRBand band = section.getBands()[0];

		// clear band
		band.getChildren().clear();

		// add textfields
		final int horizontalStep = Math.round(jasperDesign.getPageWidth() / attributes.size());
		int x = 0;
		final int y = 2;
		for (final SubreportAttribute attribute : attributes) {
			final CMAttribute cmAttribute = attribute.getCMDBuildAttribute();
			if (cmAttribute == null) {
				continue;
			}

			String name;
			if (attribute.getQueryName() != null) {
				name = attribute.getQueryName();
			} else {
				name = attribute.getCMDBuildAttribute().getName();
			}

			final JRDesignTextField tf = createTextFieldForAttribute(name, attribute.getCMDBuildAttribute().getType());
			tf.setHeight(20);
			tf.setWidth(horizontalStep);
			tf.setX(x);
			tf.setY(y);
			band.getChildren().add(tf);

			x += horizontalStep;
		}
	}

}
