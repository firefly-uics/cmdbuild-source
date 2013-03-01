package org.cmdbuild.elements.report;

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

import org.cmdbuild.dao.backend.postgresql.RelationQueryBuilder;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.TranslationService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

public class ReportFactoryTemplateDetailSubreport extends ReportFactoryTemplate {

	private List<SubreportAttribute> attributes;
	private String designTitle;
	private final JasperDesign jasperDesign;
	private final ReportExtension reportExtension;
	private final static String REPORT = "CMDBuild_card_detail_subreport.jrxml";

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

	public ReportFactoryTemplateDetailSubreport(final SubreportType subreportType, final HttpSession session,
			final ICard card, final UserContext userCtx) throws JRException {
		// init vars
		this.reportExtension = ReportExtension.PDF;
		String query = "";

		switch (subreportType) {
		case RELATIONS:
			query = new RelationQueryBuilder().buildSelectQuery(UserOperations.from(userCtx).relations().list()
					.card(card));
			final String lang = new SessionVars().getLanguage();
			designTitle = TranslationService.getInstance().getTranslation(lang, "management.modcard.tabs.relations");
			attributes = new LinkedList<SubreportAttribute>();
			attributes.add(new SubreportAttribute("domaindescription", TranslationService.getInstance().getTranslation(
					lang, "management.modcard.relation_columns.domain"), card.getSchema().getAttribute(
					CardAttributes.Description.toString())));
			attributes.add(new SubreportAttribute("classdescription", TranslationService.getInstance().getTranslation(
					lang, "management.modcard.relation_columns.destclass"), card.getSchema().getAttribute(
					CardAttributes.Description.toString())));
			attributes.add(new SubreportAttribute("begindate", TranslationService.getInstance().getTranslation(lang,
					"management.modcard.relation_columns.begin_date"), card.getSchema().getAttribute(
					CardAttributes.BeginDate.toString())));
			attributes.add(new SubreportAttribute("fieldcode", TranslationService.getInstance().getTranslation(lang,
					"management.modcard.relation_columns.code"), card.getSchema().getAttribute(
					CardAttributes.Code.toString())));
			attributes.add(new SubreportAttribute("fielddescription", TranslationService.getInstance().getTranslation(
					lang, "management.modcard.relation_columns.description"), card.getSchema().getAttribute(
					CardAttributes.Description.toString())));
			break;
		/*
		 * case HISTORY: query =
		 * userCtx.tables().get(card.getIdClass()).cards().
		 * list().history(card.getId()).toSQL(new CardQueryBuilder());
		 * designTitle = "test"; attributes = new
		 * LinkedList<SubreportAttribute>(); attributes.add(new
		 * SubreportAttribute("Dipendente_BeginDate", "titolo1",
		 * card.getSchema().getAttribute(CardAttributes.Code.toString())));
		 * attributes.add(new SubreportAttribute("Dipendente_EndDate",
		 * "titolo2",
		 * card.getSchema().getAttribute(CardAttributes.Description.toString
		 * ()))); break;
		 */
		}

		// load design
		jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT);

		// initialize design
		initDesign(query);
	}

	public JasperReport compileReport() throws JRException {
		JasperReport subreport = null;
		ByteArrayOutputStream baos = null;
		ByteArrayInputStream bais = null;
		try {
			baos = new ByteArrayOutputStream();
			JasperCompileManager.compileReportToStream(jasperDesign, baos);
			baos.flush();
			bais = new ByteArrayInputStream(baos.toByteArray());
			subreport = (JasperReport) JRLoader.loadObject(bais);
		} catch (final IOException e) {
			try {
				if (baos != null)
					baos.close();
				if (bais != null)
					bais.close();
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
				name = attribute.getIAttribute().getName();
			}
			addField(name, attribute.getIAttribute().getDescription(), attribute.getIAttribute().getType());
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
				name = attribute.getIAttribute().getName();
			}
			final JRDesignTextField tf = createTextFieldForAttribute(name, attribute.getIAttribute().getType());
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
		private final IAttribute iAttribute;

		public SubreportAttribute(final String queryName, final String label, final IAttribute attribute) {
			super();
			this.queryName = queryName;
			this.label = label;
			iAttribute = attribute;
		}

		public String getQueryName() {
			return queryName;
		}

		public String getLabel() {
			return label;
		}

		public IAttribute getIAttribute() {
			return iAttribute;
		}
	}
}
