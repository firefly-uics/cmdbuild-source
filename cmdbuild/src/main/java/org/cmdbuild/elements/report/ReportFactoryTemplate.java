package org.cmdbuild.elements.report;

import java.awt.Color;
import java.io.File;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignSubreport;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.services.Settings;

public abstract class ReportFactoryTemplate extends ReportFactory {
	
	private static final String REPORT_DIR_NAME = "WEB-INF/reports";		
	private Map<String, Object> jasperFillManagerParameters;
	
	public abstract JasperDesign getJasperDesign();

	/**
	 * Fill report 
	 */
	public JasperPrint fillReport() throws Exception {
		JasperReport newjr = JasperCompileManager.compileReport(getJasperDesign());		
		super.fillReport(newjr,jasperFillManagerParameters);
		return jasperPrint;
	}
	
	public String getReportDirectory() {
		Settings settings = Settings.getInstance();		
		return settings.getRootPath() + REPORT_DIR_NAME + File.separator;
	}

	protected void setQuery(final String reportQuery) {
		final JRDesignQuery designQuery = new JRDesignQuery();
		designQuery.setText(reportQuery);
		getJasperDesign().setQuery(designQuery);		
	}
	
	protected String getAttributeName(String attributeName, AttributeType attributeType) {
		if(attributeType.equals(AttributeType.REFERENCE) || attributeType.equals(AttributeType.LOOKUP)) {
			attributeName += "_Description";
		}
		return attributeName;
	}

	protected JRDesignTextField createTextFieldForAttribute(String attributeName, AttributeType attributeType){
		JRDesignExpression varExpr = new JRDesignExpression();
		varExpr.setValueClassName(getAttributeClass(attributeType).getName());
		varExpr.setText("$F{"+getAttributeName(attributeName, attributeType)+"}");
		JRDesignTextField field = new JRDesignTextField();
		field.setExpression(varExpr);
		field.setBlankWhenNull(true);
		field.setStretchWithOverflow(true);
		field.setForecolor(Color.BLACK);
		field.setBackcolor(Color.GRAY);
		field.setPositionType(JRDesignTextField.POSITION_TYPE_FLOAT);
		field.setX(0);
		field.setY(0);
		return field;
	}		
	
	protected JRDesignStaticText createStaticTextForAttribute(IAttribute iAttribute) {
		JRDesignStaticText dst = new JRDesignStaticText();
		String labelText;
		if(iAttribute.getDescription()!=null && !iAttribute.getDescription().equals("")) {
			labelText = iAttribute.getDescription();
		} else {
			labelText = iAttribute.getDBName();
		}
		dst.setPositionType(JRDesignStaticText.POSITION_TYPE_FLOAT);
		dst.setText(labelText);
		dst.setHeight(20);
		dst.setWidth(100);		
		
		return dst;
	}
	
	protected JRDesignStaticText createStaticText(String text) {
		JRDesignStaticText dst = new JRDesignStaticText();			
		dst.setText(text);
		dst.setPositionType(JRDesignStaticText.POSITION_TYPE_FLOAT);
		dst.setHeight(20);
		dst.setWidth(100);
		
		return dst;
	} 	
		
	protected Class<?> getAttributeClass(AttributeType type) {
		switch(type) {
			case DOUBLE:
				return Double.class;
			case DECIMAL:
				return Double.class;
			case INTEGER:
				return Integer.class;
			case TIMESTAMP:
				return Timestamp.class;
			case DATE:
				return Date.class;
			case BOOLEAN:
				return Boolean.class;
			default:
				return String.class;
		}
	}
	
	/**
	 * Set report title (custom string)
	 */
	@SuppressWarnings("unchecked")
	protected void setTitle(String title) {
		Object obj = null;
		JRDesignStaticText field = null;
		JRBand titleBand = getJasperDesign().getTitle();
		List<Object> f = titleBand.getChildren();
		Iterator<Object> it = f.iterator();
		
		while (it.hasNext()) {
			obj = it.next();
			if (obj instanceof JRDesignStaticText) {
				String reportTitle = "";
				if(title!=null && !title.equals("")) {
					reportTitle = title;
				}
				field = (JRDesignStaticText) obj;
				field.setText(reportTitle);
			}
		}
	}

	
	/**
	 * Add string parameter to design
	 * 
	 * @param parametersMap
	 * @throws JRException
	 */
	protected void addDesignParameter(String name, String defaultvalue) throws JRException {
		JRDesignParameter jrParam = new JRDesignParameter();
		jrParam.setName(name);
		jrParam.setForPrompting(false);
		jrParam.setValueClass(String.class);
		JRDesignExpression exp = new JRDesignExpression();
		exp.setText("\""+defaultvalue+"\"");
		exp.setValueClass(String.class);
		jrParam.setDefaultValueExpression(exp);
		getJasperDesign().addParameter(jrParam);
	}
	
	protected void addFillParameter(String key, Object value) throws JRException {
		if(jasperFillManagerParameters==null) {
			jasperFillManagerParameters = new LinkedHashMap<String, Object>();
		}
		jasperFillManagerParameters.put(key,value);
	}
	
	/**
	 * Update images path only in title band; images are supposed to be in the same folder of master report
	 */
	@SuppressWarnings("unchecked")
	protected void updateImagesPath() {
		Object obj = null;
		JRBand title = getJasperDesign().getTitle();
		List<Object> f = title.getChildren();
		Iterator<Object> it = f.iterator();
		
		while (it.hasNext()) {
			obj = it.next();
			if (obj instanceof JRDesignImage) {
				JRDesignImage img = (JRDesignImage) obj;
				JRDesignExpression varExp = (JRDesignExpression)img.getExpression();
				String path = "\""+ getReportDirectory() + varExp.getText().substring(1,varExp.getText().length()-1)+"\"";
				path = escapeWinSeparators(path);
				varExp.setText(path);
			}
		}
	}
	
	/**
	 * Update subreports path (in every JRBand); subreports are supposed to be in the same folder of master report
	 */
	@SuppressWarnings("unchecked")
	protected void updateSubreportsPath() {
		List<JRBand> bands = getBands(getJasperDesign());
		
		for(JRBand band : bands) {
			if(band!=null) {
				List<Object> f = band.getChildren();
				Iterator<Object> it = f.iterator();		
				
				Object obj = null;
				while (it.hasNext()) {
					obj = it.next();
					if (obj instanceof JRDesignSubreport) {
						JRDesignSubreport subreport = (JRDesignSubreport) obj;
						JRDesignExpression varExp = (JRDesignExpression)subreport.getExpression();
						String path = "\""+ getReportDirectory() + varExp.getText().substring(1,varExp.getText().length()-1)+"\"";
						path = escapeWinSeparators(path);
						varExp.setText(path);
					}
				}	
			}
		}
	}
	
	/**
	 * Create report fields 
	 */
	protected void setFields(Collection<IAttribute> attributes) throws JRException {
		deleteAllFields();
		for(IAttribute iAttribute : attributes) {
			getJasperDesign().addField(createDesignField(iAttribute));
		}
	}
	
	protected void addField(String name, String description, AttributeType attributeType) throws JRException {
		getJasperDesign().addField(createDesignField(name,description,attributeType));
	}		
	
	/**
	 * Remove all existing fields
	 */
	protected void deleteAllFields() {
		JRField[] list = getJasperDesign().getFields();
		for(JRField field : list) {
			getJasperDesign().removeField(field);
		}
	}
	
	/**
	 * Create report field for attribute 
	 */
	private JRDesignField createDesignField(IAttribute iAttribute){
		JRDesignField field = new JRDesignField();
		field.setName(getAttributeName(iAttribute.getSchema().getName()+"_"+iAttribute.getName(), iAttribute.getType()));
		field.setDescription(iAttribute.getDescription());
		field.setValueClassName(getAttributeClass(iAttribute.getType()).getName());		
		return field;
	}
	
	private JRDesignField createDesignField(String name, String description, AttributeType attributeType) {
		JRDesignField field = new JRDesignField();
		field.setName(name);
		field.setDescription(description);
		field.setValueClassName(getAttributeClass(attributeType).getName());
		return field;
	}

	private String escapeWinSeparators(String path) {
		StringBuffer newpath = new StringBuffer();
		char sep = '\\';
		if (File.separator.toCharArray()[0]==sep) {
			char[] ca = path.toCharArray();
			char ct;
			for (int i=0;i<ca.length;i++){
				ct = ca[i];
				if (ct!=sep){
					newpath.append(ct);
				} else {
					newpath.append(sep);
					newpath.append(ct);
				}
			}
			path = newpath.toString();
		}
		return path;
	}	

}
