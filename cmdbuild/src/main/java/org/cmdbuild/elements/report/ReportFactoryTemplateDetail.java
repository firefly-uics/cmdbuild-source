package org.cmdbuild.elements.report;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRCommonText;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignLine;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.dao.backend.postgresql.CardQueryBuilder;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.report.ReportFactoryTemplateDetailSubreport.SubreportType;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.TranslationService;
import org.cmdbuild.services.auth.UserContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportFactoryTemplateDetail extends ReportFactoryTemplate {

	@Autowired
	private CMBackend backend = CMBackend.INSTANCE;

	private ICard card;
	private UserContext userCtx;
	private String designTitle;
	private HttpSession session;
	private JasperDesign jasperDesign;
	private ITable table;
	private ReportExtension reportExtension;
	private final static String REPORT = "CMDBuild_card_detail.jrxml";

	public ReportFactoryTemplateDetail(ICard card, UserContext userCtx, ReportExtension reportExtension) throws JRException {
		// init vars
		this.card = card;
		this.userCtx = userCtx;
		this.reportExtension = reportExtension;
		this.table = card.getSchema();
		CardQueryBuilder qb = new CardQueryBuilder();
		String query = backend.cardQueryToSQL(card.getSchema().cards().list().id(card.getId()), qb);
		designTitle = TranslationService.getInstance().getTranslation(new SessionVars().getLanguage(),"management.modcard.tabs.card");
		
		// load design
		this.jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT);
		
		// initialize design
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
	
	private void initDesign(String query) throws JRException {
		// set name
		jasperDesign.setName(table.getName());
		
		// set report query
		setQuery(query); 
		
		// set report fields
		setFields(table.getAttributes().values());
		
		// set detail band
		setDetail();
			
		// title
		setTitle(table.getName() + " - " + card.getDescription());
		
		// images
		updateImagesPath();
		
		// paramenters		
		addDesignParameter("Card_Detail_Title", designTitle);
		addFillParameter("Card_Detail_Title", designTitle);
		
		// relations subreport
		setRelationsSubreport();
	}	

	/**
	 * Add relations subreport to fill parameters
	 * @throws JRException
	 */
	private void setRelationsSubreport() throws JRException {
		ReportFactoryTemplateDetailSubreport rftds = new ReportFactoryTemplateDetailSubreport(SubreportType.RELATIONS, session, card, userCtx);
		JasperReport compiledSubreport = rftds.compileReport();
		addFillParameter("relations_subreport", compiledSubreport);
	}
	
	@SuppressWarnings("unchecked")
	private void setDetail() {
		
		// get (sorted) list of attributes
		List<IAttribute> attributesToShow = new LinkedList<IAttribute>();
		IAttribute notes = null;
		for(IAttribute iAttribute : table.getAttributes().values()) {			
			if(iAttribute.isDisplayable()) {
				attributesToShow.add(iAttribute);
				if(isNotesAttribute(iAttribute)) {
					notes = iAttribute;
				}
			}
		}
		Collections.sort(attributesToShow, new IAttributeComparator());
		
		// place notes at the end
		if(notes!=null) {
			attributesToShow.remove(notes);
			attributesToShow.add(notes);
		}
		
		// clear band
		JRSection section = getJasperDesign().getDetailSection();
		JRBand band = section.getBands()[0];				
		band.getChildren().clear();
						
		// add textfields
		int x = 0;
		int y = 0;
		int width = jasperDesign.getPageWidth() - (30 * 2); // 30 = page margin
		int height = 20;
		int verticalStep = 20;
		for(IAttribute iAttribute : attributesToShow) {
			
			// print line for notes attrib
			if(isNotesAttribute(iAttribute)) {				
				JRDesignLine line = new JRDesignLine();
				line.setX(x);
				line.setY(y);
				line.setHeight(1);
				line.setWidth(width);
				line.setPositionType(JRDesignLine.POSITION_TYPE_FLOAT);
				band.getChildren().add(line);
				y+= (verticalStep/2);
			}
			
			// print textfield
			JRDesignTextField tf = createTextFieldForAttribute(iAttribute);
			tf.setHeight(height);
			tf.setWidth(width);
			tf.setX(x);
			tf.setY(y);
			band.getChildren().add(tf);

			y+=verticalStep;
		}
		
		// update band height
		int detailHeight = y + 5;
		JRDesignBand db = (JRDesignBand) band;
		db.setHeight(detailHeight);
		
		// update page height (if necessary)
		int totBandsHeight = 0;
		for(JRBand myBand : getBands(jasperDesign)) {
			if(myBand!=null)
				totBandsHeight += myBand.getHeight();
		}
		if(totBandsHeight > jasperDesign.getPageHeight()) {
			jasperDesign.setPageHeight(totBandsHeight);
		}
	}
	
	/**
	 * Create a texfield with an expression like: 
	 * msg("Descrizione : {0}",$F{Computer_Description}).equals("Descrizione : null")?"Descrizione : ":msg("Descrizione : {0}",$F{Computer_Description})
	 * 
	 */
	private JRDesignTextField createTextFieldForAttribute(IAttribute iAttribute) {
		// get default texfield
		JRDesignTextField dtf = super.createTextFieldForAttribute(iAttribute.getSchema().getName()+"_"+iAttribute.getName(), iAttribute.getType());
		
		// customize expression
		String label, fieldattname;
		if(iAttribute.getDescription()!=null && !iAttribute.getDescription().equals("")) {
			label = iAttribute.getDescription();
		} else {
			label = iAttribute.getDBName();
		}
		label = label + " : "; // ie - Descrizione : null
		fieldattname = "$F{"+getAttributeName(table.getName()+"_"+iAttribute.getName(), iAttribute.getType())+"}"; //ie - $F{Computer_Description}
		String fieldmsg = "msg(\""+label + "{0}\"," + fieldattname + ")"; //ie - msg("Descrizione : {0}",$F{Computer_Description})
		String fieldnull = label+"null"; //ie - Descrizione : null
		String completeexp = fieldmsg+".equals(\""+fieldnull+"\")?\""+label+"\":"+fieldmsg; //ie - msg("Descrizione : {0}",$F{Computer_Description}).equals("Descrizione : null")?"Descrizione : ":msg("Descrizione : {0}",$F{Computer_Description})
		JRDesignExpression exp = new JRDesignExpression();
		exp.setValueClass(String.class);		
		exp.setText(completeexp);
		dtf.setExpression(exp);
		dtf.setMarkup(JRCommonText.MARKUP_HTML);
		
		return dtf;
	}
	
	private boolean isNotesAttribute(IAttribute iAttribute) {
		return iAttribute.getDBName().equals((card.getSchema().getAttribute(CardAttributes.Notes.toString()).getDBName()));
	}
	
	private class IAttributeComparator implements Comparator<IAttribute> {
		public int compare(IAttribute a1, IAttribute a2) {
			if(a1.getIndex() > a2.getIndex())
				return 1;
			else if(a1.getIndex() < a2.getIndex())
				return -1;
			else
				return 0;
		}
	}
}
