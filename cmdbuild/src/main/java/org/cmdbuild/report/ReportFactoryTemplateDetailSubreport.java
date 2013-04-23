package org.cmdbuild.report;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.TranslationService;

public class ReportFactoryTemplateDetailSubreport extends ReportFactoryTemplate {

	private List<SubreportAttribute> attributes;
	private String designTitle;
	private final JasperDesign jasperDesign;
	private final ReportExtension reportExtension;
	private final static String REPORT = "CMDBuild_card_detail_subreport.jrxml";
	private final static String DESCRIPTION = "Description";
	private final static String BEGIN_DATE = "BeginDate";
	private final static String CODE = "Code";

	public enum SubreportType {
		RELATIONS, HISTORY
	}

	@Override
	public JasperDesign getJasperDesign() {
		return jasperDesign;
	}

	@Override
	public ReportExtension getReportExtension() {
		return reportExtension;
	}

	public ReportFactoryTemplateDetailSubreport( //
			final SubreportType subreportType, //
			final HttpSession session, //
			final CMClass table, //
			final Card card) throws JRException {

		// init vars
		this.reportExtension = ReportExtension.PDF;

		String query = "";
		switch (subreportType) {

		case RELATIONS:

			// TODO extract the query for relations
			// query = new
			// RelationQueryBuilder().buildSelectQuery(UserOperations.from(userCtx).relations().list().card(card));

			query = "SELECT * FROM " + table.getIdentifier().getLocalName();

			designTitle = getTranslation("management.modcard.tabs.relations");
			attributes = new LinkedList<SubreportAttribute>();
			attributes.add( //
					new SubreportAttribute("domaindescription", //
						getTranslation("management.modcard.relation_columns.domain"), //
						table.getAttribute(DESCRIPTION) //
						) //
					);

			attributes.add( //
					new SubreportAttribute("classdescription", //
						getTranslation("management.modcard.relation_columns.destclass"), //
						table.getAttribute(DESCRIPTION) //
						) //
					);

			// FIXME begin date is reserved
//			attributes.add( //
//					new SubreportAttribute("begindate", //
//						getTranslation("management.modcard.relation_columns.begin_date"), //
//						table.getAttribute(BEGIN_DATE) //
//						) //
//					);

			attributes.add( //
					new SubreportAttribute("fieldcode", //
							getTranslation("management.modcard.relation_columns.code"), //
							table.getAttribute(CODE)
							) //
					);//

			attributes.add( //
					new SubreportAttribute("fielddescription", //
							getTranslation("management.modcard.relation_columns.description"), //
							table.getAttribute(DESCRIPTION) //
							) //
						);
			break;

		case HISTORY:
			query = "SELECT * FROM " + table.getIdentifier().getLocalName();
//			query = userCtx.tables().get(card.getIdClass()).cards().list().history(card.getId()).toSQL(new CardQueryBuilder());

			designTitle = "test";
			attributes = new LinkedList<SubreportAttribute>();

			attributes.add( //
					new SubreportAttribute("Dipendente_BeginDate", //
					"titolo1", //
					table.getAttribute(CODE) //
					) //
				);

			attributes.add(new SubreportAttribute("Dipendente_EndDate", //
					"titolo2", //
					table.getAttribute(DESCRIPTION) //
					) //
			);

			break;
		}

		// load design
		jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT);

		// initialize design
		initDesign(query);
	}

	private String getTranslation(final String key) {
		final String lang = new SessionVars().getLanguage();
		return TranslationService.getInstance().getTranslation(lang, key);
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
			String name;
			if (attribute.getQueryName() != null) {
				name = attribute.getQueryName();
			} else {
				name = attribute.getCMDBuildAttribute().getName();
			}
			addField(name, attribute.getCMDBuildAttribute().getDescription(), attribute.getCMDBuildAttribute().getType());
		}

		// set column header
		setColumnHeader();

		// set detail
		setDetail();

		// set design parameters
		addDesignParameter("title", designTitle);
	}

	@SuppressWarnings("unchecked")
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
			st.setMode(JRElement.MODE_OPAQUE);
			st.setBackcolor(new Color(236, 236, 236)); // gray
			st.setHeight(20);
			st.setWidth(horizontalStep);
			st.setX(x);
			st.setY(y);
			band.getChildren().add(st);

			x += horizontalStep;
		}
	}

	@SuppressWarnings("unchecked")
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

	private class SubreportAttribute {
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
}
