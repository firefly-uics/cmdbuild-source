package org.cmdbuild.elements.report;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.logger.Log;

public class RPSimple extends ReportParameter {	

	protected RPSimple(JRParameter jrParameter) {
		super();
		setJrParameter(jrParameter);
		if(getJrParameter()==null || getFullName()==null || getFullName().equals(""))
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
	}
	
	public void parseValue(String newValue) {		
		try {			
			if(newValue!=null && !newValue.equals("")) {
							
				if(getJrParameter().getValueClass() == String.class)
					setValue(newValue);
				
				else if(getJrParameter().getValueClass() == Integer.class || 
						getJrParameter().getValueClass() == Number.class)
					setValue(Integer.parseInt(newValue));
				
				else if(getJrParameter().getValueClass() == Long.class)
					setValue(Long.parseLong(newValue));
				
				else if(getJrParameter().getValueClass() == Short.class)
					setValue(Short.parseShort(newValue));
				
				else if(getJrParameter().getValueClass() == BigDecimal.class)
					setValue(new BigDecimal(Integer.parseInt(newValue)));						
				
				else if(getJrParameter().getValueClass() == Date.class)
					setValue(new SimpleDateFormat("dd/MM/yy").parse(newValue));
					
				else if(getJrParameter().getValueClass() == Timestamp.class) {
					Date date = new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse(newValue);
					setValue(new Timestamp(date.getTime()));
				}
				
				else if(getJrParameter().getValueClass() == Time.class) {
					Date date = new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse(newValue);
					setValue(new Time(date.getTime()));
				}						
					
				else if(getJrParameter().getValueClass() == Double.class)
					setValue(Double.parseDouble(newValue));
				
				else if(getJrParameter().getValueClass() == Float.class)
					setValue(Float.parseFloat(newValue));
				
				else if(getJrParameter().getValueClass() == Boolean.class)
					setValue(Boolean.parseBoolean(newValue));
				
				else {
					throw ReportExceptionType.REPORT_INVALID_PARAMETER_CLASS.createException();
				}
			}
		} catch (Exception e) {
			Log.REPORT.error("Invalid parameter value \""+newValue+"\" for \""+getJrParameter().getValueClass()+"\"", e);
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_VALUE.createException();
		}
	}
	
	public IAttribute createCMDBuildAttribute(ITableFactory tf) {		
		IAttribute attribute;

		// set class
		if (getJrParameter().getValueClass() == String.class) {				
			attribute = createCMDBuildAttribute(tf, AttributeType.STRING);
			attribute.setLength(80);			
		} else if (getJrParameter().getValueClass() == Integer.class || 
				getJrParameter().getValueClass() == Long.class || 
				getJrParameter().getValueClass() == Short.class || 
				getJrParameter().getValueClass() == BigDecimal.class || 
				getJrParameter().getValueClass() == Number.class) {			
			attribute = createCMDBuildAttribute(tf, AttributeType.INTEGER);
			attribute.setLength(20);
		} else if (getJrParameter().getValueClass() == Date.class) {
			attribute = createCMDBuildAttribute(tf, AttributeType.DATE);
		} else if (getJrParameter().getValueClass() == Timestamp.class || 
				getJrParameter().getValueClass() == Time.class) {
			attribute = createCMDBuildAttribute(tf, AttributeType.TIMESTAMP);
		} else if (getJrParameter().getValueClass() == Double.class || 
				getJrParameter().getValueClass() == Float.class) {
			attribute = createCMDBuildAttribute(tf, AttributeType.DOUBLE);
			attribute.setLength(20);
		} else if (getJrParameter().getValueClass() == Boolean.class) {
			attribute = createCMDBuildAttribute(tf, AttributeType.BOOLEAN);
		} else {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_CLASS.createException();
		}
		
		//set default value
		if(hasDefaultValue()) {
			attribute.setDefaultValue(getDefaultValue());
		}
		
		return attribute;
	}
}
