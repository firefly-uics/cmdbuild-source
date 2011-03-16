package org.cmdbuild.elements.report;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.logger.Log;

public class ReportFactoryTemplateList extends ReportFactoryTemplate {
	
	private List<String> attributeOrder;
	private JasperDesign jasperDesign;
	private ReportExtension reportExtension;
	private ITable table;
	private final static String REPORT_PDF = "CMDBuild_list.jrxml";
	private final static String REPORT_CSV = "CMDBuild_list_csv.jrxml";

	public ReportFactoryTemplateList(ReportExtension reportExtension, String query, List<String> attributeOrder, ITable table) throws JRException {
		
		// init vars
		this.reportExtension = reportExtension;
		this.attributeOrder = attributeOrder;
		this.table = table;				
		
		// load design
		if(reportExtension == ReportExtension.PDF)
			this.jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT_PDF);
		else
			this.jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT_CSV);
		
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
		
		// set query
		setQuery(query);
		
		// set report fields
		List<IAttribute> fields = new LinkedList<IAttribute>();
		for(String attribute : attributeOrder) {
			fields.add(table.getAttribute(attribute));
		}
		setFields(fields);
		
		// set report text-fields in detail band
		setTextFields();
		
		// set column headers for new fields
		setColumnHeaders();
				
		if(reportExtension == ReportExtension.PDF) {
			// update report title
			setTitle(table.getName());
			// update images path
			updateImagesPath();
		}
		
		// refresh layout
		updateDesign();
	}
	
	/*
	 * Create text-fields in detail band
	 */
	@SuppressWarnings("unchecked")	
	private void setTextFields() {
		// CREATE TEXTFIELDS	
		JRSection section = jasperDesign.getDetailSection();
		JRBand band = section.getBands()[0];
		List<Object> f = band.getChildren();
		Iterator<Object> it = f.iterator();
		Vector<Object> detailVector = new Vector<Object>();
		Vector<Object> graphicVector = new Vector<Object>();
		Object obj = null;
		
		// backup graphics elements
		while (it.hasNext()){
			obj = it.next();
			if (!(obj instanceof JRDesignTextField)) {
				graphicVector.add(obj);
			}
		}
		
		// create detail text fields
		for(String attribute : attributeOrder) {
			IAttribute ab = table.getAttribute(attribute);
			detailVector.add(createTextFieldForAttribute(table.getName()+"_"+ab.getName(), ab.getType()));
		}		
		
		// saving new list of items
		band.getChildren().clear();
		band.getChildren().addAll(graphicVector);
		band.getChildren().addAll(detailVector);
	}
	
	/*
	 * Set column headers
	 */
	@SuppressWarnings("unchecked")
	private void setColumnHeaders() {		
		JRBand columnHeader = jasperDesign.getColumnHeader();
		JRElement[] elements = columnHeader.getElements();
		Vector<JRElement> designHeaders = new Vector<JRElement>();
		Vector<JRElement> designElements = new Vector<JRElement>();
		
		// backup existing design elements
		for (int i=0;i<elements.length;i++ ) {
			if (!(elements[i] instanceof JRDesignStaticText)) {
				designElements.add(elements[i]);
			}
		}
		
		// create column headers
		for(String attribute : attributeOrder) {
			IAttribute iAttribute = table.getAttribute(attribute);
			JRDesignStaticText dst = new JRDesignStaticText();
			dst.setText(iAttribute.getDescription());
			designHeaders.add(dst);
		}
		
		// save new list of items
		columnHeader.getChildren().clear();
		columnHeader.getChildren().addAll(designElements);
		columnHeader.getChildren().addAll(designHeaders);
	}

	/*
	 * Update position of report elements
	 */
	@SuppressWarnings("unchecked")
	private void updateDesign() {
		
		// calculate weight of all elements
		Hashtable weight = new Hashtable();
		IAttribute ab = null;
		int virtualWidth = 0;
		int size = 0;
		int height= 17;
		String key = "";
		for(String attribute : attributeOrder) {
			ab = table.getAttribute(attribute);
			
			if(ab.getType() == AttributeType.BOOLEAN) size=4;
			else if(ab.getType() == AttributeType.INTEGER) size=8;
			else if(ab.getType() == AttributeType.DECIMAL) size=8;
			else if(ab.getType() == AttributeType.DOUBLE) size=8;
			else if(ab.getType() == AttributeType.DATE) size=10;
			else if(ab.getType() == AttributeType.TIMESTAMP) size=16;
			else if(ab.getType() == AttributeType.LOOKUP) size=20;
			else if(ab.getType() == AttributeType.REFERENCE) size=20;
			else if(ab.getType() == AttributeType.TEXT) size=50;
			else if(ab.getType() == AttributeType.STRING) size=(ab.getLength()>4?ab.getLength():4)>40?40:(ab.getLength()>4?ab.getLength():4);
			else size=20;
			
			virtualWidth += size;
			key = getAttributeName(table.getName()+"_"+ab.getName(), ab.getType());
			weight.put(ab.getName(),size+"");
			weight.put(key,size+"");
			weight.put(ab.getDescription(), size+"");
		}
		int pageWidth = jasperDesign.getPageWidth();
		double cx = ((double)pageWidth*0.95) / ((double)virtualWidth);
		Log.REPORT.debug("cx="+cx+" pageWidth "+((double)pageWidth*0.95) +" / virtualWidth "+virtualWidth);
		double doub = 0;
		JRSection section = jasperDesign.getDetailSection();
		JRBand detail = section.getBands()[0];
		JRElement[] elements = detail.getElements();
		JRDesignTextField dtf = null;
		int x = 0;
		int y = 2;
		Log.REPORT.debug("RF updateDesign DESIGN");
		JRDesignExpression varExpr = null;
		for (int i=0;i<elements.length;i++ ) {
			if (elements[i] instanceof JRDesignTextField) {
				dtf = (JRDesignTextField) elements[i];
				varExpr = (JRDesignExpression) dtf.getExpression();
				key = varExpr.getText();
				Log.REPORT.debug("text="+key);
				key = key.substring(3,key.length()-1);
				Log.REPORT.debug("text="+key);
				key = (String) weight.get(key);
				Log.REPORT.debug("kry="+key);
				try {
					size = Integer.parseInt(key);
				} catch (NumberFormatException e) {
					size = 0;
				}
				doub = ((double)size)*cx ;
				size = (int) doub;
				dtf.setX(x);
				dtf.setY(y);
				dtf.setWidth(size);
				dtf.setHeight(height);
				dtf.setBlankWhenNull(true);
				dtf.setStretchWithOverflow(true);
				Log.REPORT.debug("RF updateDesign x="+dtf.getX()+" Width="+dtf.getWidth());
				x += size;
			} 
		}
		
		// sizing table headers
		JRBand columnHeader = jasperDesign.getColumnHeader();
		elements = columnHeader.getElements();
		JRDesignStaticText dst = null;
		x = 0;
		Log.REPORT.debug("RF updateDesign HEADER");
		for (int i=0;i<elements.length;i++ ) {
			if (elements[i] instanceof JRDesignStaticText) {
				dst = (JRDesignStaticText) elements[i];
				key = dst.getText();
				Log.REPORT.debug("text="+key);
				key = (String) weight.get(key);
				Log.REPORT.debug("key="+key);				
				size = Integer.parseInt(key);
				
				doub = ((double)size)*cx ;
				size = (int) doub;
				dst.setForecolor(Color.WHITE);
				dst.setX(x);
				dst.setHeight(height);
				dst.setWidth(size);
				Log.REPORT.debug("RF updateDesign"+dst.getText()+" x="+dst.getX()+" Width="+dst.getWidth());
				x += size;
			}
		}
	}	
}
