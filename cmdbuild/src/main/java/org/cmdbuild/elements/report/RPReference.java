package org.cmdbuild.elements.report;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.services.auth.UserContext;

public class RPReference extends ReportParameter {	

	protected RPReference(JRParameter jrParameter) {
		super();
		setJrParameter(jrParameter);
		if(getJrParameter()==null || getFullName()==null || getFullName().equals("") || !getFullName().matches( regExpLR ))
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		if(getJrParameter().getValueClass() != Integer.class)
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_REFERENCE_CLASS.createException();
		
		// try to read table for class		
		try {
			UserContext.systemContext().tables().get(getClassName());
		} catch (NotFoundException e) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_CMDBUILD_CLASS.createException();
		}
		
		// try to read attribute
		try {
			UserContext.systemContext().tables().get(getClassName()).getAttribute(getAttributeName());
		} catch (NotFoundException e) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_CMDBUILD_ATTRIBUTE.createException();
		}
	}
	
	public IAttribute createCMDBuildAttribute(ITableFactory tf) {		
		IAttribute attribute = createCMDBuildAttribute(tf, AttributeType.REFERENCE);
		
		//table
		ITable table =  tf.get(getClassName());
		
		//domain
		IDomain domain = UserContext.systemContext().domains().create();
		domain.setClass2(table);
		
		//attribute
		attribute.setIsReferenceDirect(true);
		attribute.setReferenceDomain(domain);
		
		return attribute;
	}
	
	public String getClassName() {
		return getFullNameSplit()[1];
	}

	public String getAttributeName() {
		return getFullNameSplit()[2];
	}
	
	public void parseValue(String value) {
		if(value!=null && !value.equals(""))
			setValue(Integer.parseInt(value));
	}
	
}
