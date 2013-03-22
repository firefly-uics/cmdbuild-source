package org.cmdbuild.elements.report;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRCommonText;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignLine;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.elements.report.ReportFactoryTemplateDetailSubreport.SubreportType;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.TranslationService;
import org.cmdbuild.services.auth.UserContext;

public class ReportFactoryTemplateDetail extends ReportFactoryTemplate {

	private final Card card;
	private final UserContext userCtx;
	private final String designTitle;
	private HttpSession session;
	private final JasperDesign jasperDesign;
	private final CMClass table;
	private final ReportExtension reportExtension;

	private final static String REPORT = "CMDBuild_card_detail.jrxml";
	private final static String NOTES = "Notes"; // there is a way in the new
													// DAO to know the notes
													// attribute name?

	public ReportFactoryTemplateDetail(final String className, final Long cardId, final UserContext userCtx,
			final ReportExtension reportExtension) throws JRException {
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final CMDataView dataView = TemporaryObjectsBeforeSpringDI.getSystemView();

		this.userCtx = userCtx;
		this.reportExtension = reportExtension;

		card = dataAccessLogic.fetchCard(className, cardId);
		table = dataAccessLogic.findClass(className);
		designTitle = TranslationService.getInstance().getTranslation(new SessionVars().getLanguage(),
				"management.modcard.tabs.card");

		// load design
		this.jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT);

		// initialize design with the query
		final QuerySpecsBuilder querySpecBuilder = dataView.select(anyAttribute(table)) //
				.from(table) //
				.where(condition(attribute(table, "Id"), eq(cardId)));
		final String query = getQueryString(new QueryCreator(querySpecBuilder.build()));
		initDesign(query);
	}

	@Override
	public JasperDesign getJasperDesign() {
		return jasperDesign;
	}

	@Override
	public ReportExtension getReportExtension() {
		return reportExtension;
	}

	private void initDesign(final String query) throws JRException {
		final String tableName = table.getIdentifier().getLocalName();
		jasperDesign.setName(tableName);
		setQuery(query);
		setFields(table.getActiveAttributes());

		// set detail band
		setDetail();

		setTitle(tableName + " - " + card.getAttribute("Description"));
		updateImagesPath();

		// parameters
		addDesignParameter("Card_Detail_Title", designTitle);
		addFillParameter("Card_Detail_Title", designTitle);

		// relations subreport
		setRelationsSubreport();
	}

	/**
	 * Add relations subreport to fill parameters
	 * 
	 * @throws JRException
	 */
	private void setRelationsSubreport() throws JRException {
		final ReportFactoryTemplateDetailSubreport rftds = new ReportFactoryTemplateDetailSubreport( //
				SubreportType.RELATIONS, //
				session, //
				table, //
				card, //
				userCtx);
		final JasperReport compiledSubreport = rftds.compileReport();
		addFillParameter("relations_subreport", compiledSubreport);
	}

	@SuppressWarnings("unchecked")
	private void setDetail() {

		// get (sorted) list of attributes
		final List<CMAttribute> attributesToShow = new LinkedList<CMAttribute>();
		CMAttribute notes = null;
		for (final CMAttribute attribute : table.getActiveAttributes()) {
			if (attribute.isDisplayableInList()) {
				attributesToShow.add(attribute);
				if (isNotesAttribute(attribute)) {
					notes = attribute;
				}
			}
		}

		Collections.sort(attributesToShow, new CMAttributeComparator());

		// place notes at the end
		if (notes != null) {
			attributesToShow.remove(notes);
			attributesToShow.add(notes);
		}

		// clear band
		final JRSection section = getJasperDesign().getDetailSection();
		final JRBand band = section.getBands()[0];
		band.getChildren().clear();

		// add textfields
		final int x = 0;
		int y = 0;
		final int width = jasperDesign.getPageWidth() - (30 * 2); // 30 = page
																	// margin
		final int height = 20;
		final int verticalStep = 20;
		for (final CMAttribute attribute : attributesToShow) {

			// print line for notes attribute
			if (isNotesAttribute(attribute)) {
				final JRDesignLine line = new JRDesignLine();
				line.setX(x);
				line.setY(y);
				line.setHeight(1);
				line.setWidth(width);
				line.setPositionType(JRElement.POSITION_TYPE_FLOAT);
				band.getChildren().add(line);
				y += (verticalStep / 2);
			}

			// print text-field
			final JRDesignTextField tf = createTextFieldForAttribute(attribute);
			tf.setHeight(height);
			tf.setWidth(width);
			tf.setX(x);
			tf.setY(y);
			band.getChildren().add(tf);

			y += verticalStep;
		}

		// update band height
		final int detailHeight = y + 5;
		final JRDesignBand db = (JRDesignBand) band;
		db.setHeight(detailHeight);

		// update page height (if necessary)
		int totBandsHeight = 0;
		for (final JRBand myBand : getBands(jasperDesign)) {
			if (myBand != null) {
				totBandsHeight += myBand.getHeight();
			}
		}
		if (totBandsHeight > jasperDesign.getPageHeight()) {
			jasperDesign.setPageHeight(totBandsHeight);
		}
	}

	/**
	 * Create a texfield with an expression like:
	 * msg("Descrizione : {0}",$F{Computer_Description
	 * }).equals("Descrizione : null"
	 * )?"Descrizione : ":msg("Descrizione : {0}",$F{Computer_Description})
	 * 
	 */
	private JRDesignTextField createTextFieldForAttribute(final CMAttribute attribute) {
		// get default texfield
		final String attributeName = attribute.getOwner().getIdentifier().getLocalName() + "_" + attribute.getName();
		final JRDesignTextField dtf = super.createTextFieldForAttribute(attributeName, attribute.getType());

		// customize expression
		String label, fieldattname;
		if (attribute.getDescription() != null && !attribute.getDescription().equals("")) {

			label = attribute.getDescription();
		} else {
			label = attribute.getName();
		}

		label = label + " : "; // ie - Descrizione : null
		fieldattname = "$F{" + getAttributeName(table.getName() + "_" + attribute.getName(), attribute.getType()) + "}"; // ie
																															// -
																															// $F{Computer_Description}
		final String fieldmsg = "msg(\"" + label + "{0}\"," + fieldattname + ")"; // ie
		// -
		// msg("Descrizione : {0}",$F{Computer_Description})
		final String fieldnull = label + "null"; // ie - Descrizione : null
		final String completeexp = fieldmsg + ".equals(\"" + fieldnull + "\")?\"" + label + "\":" + fieldmsg; // ie
		// -
		// msg("Descrizione : {0}",$F{Computer_Description}).equals("Descrizione : null")?"Descrizione : ":msg("Descrizione : {0}",$F{Computer_Description})
		final JRDesignExpression exp = new JRDesignExpression();
		exp.setValueClass(String.class);
		exp.setText(completeexp);
		dtf.setExpression(exp);
		dtf.setMarkup(JRCommonText.MARKUP_HTML);

		return dtf;
	}

	private boolean isNotesAttribute(final CMAttribute attribute) {
		return NOTES.equals(attribute.getName());
	}

	private class CMAttributeComparator implements Comparator<CMAttribute> {
		@Override
		public int compare(final CMAttribute a1, final CMAttribute a2) {
			if (a1.getIndex() > a2.getIndex()) {
				return 1;
			} else if (a1.getIndex() < a2.getIndex()) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
