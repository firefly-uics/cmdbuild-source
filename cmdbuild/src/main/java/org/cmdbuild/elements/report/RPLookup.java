package org.cmdbuild.elements.report;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ReportException.ReportExceptionType;

public class RPLookup extends ReportParameter {

	protected RPLookup(JRParameter jrParameter) {
		super();
		setJrParameter(jrParameter);
		if (getJrParameter() == null || getFullName() == null || getFullName().equals("")
				|| !getFullName().matches(regExpLR)) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}
		if (getJrParameter().getValueClass() != Integer.class) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_LOOKUP_CLASS.createException();
		}
	}
	
	public IAttribute createCMDBuildAttribute(ITableFactory tf) {
		IAttribute attribute = createCMDBuildAttribute(tf, AttributeType.LOOKUP);
		try {
			attribute.setLookupType(getLookupName());
		} catch (NotFoundException e) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_CMDBUILD_LOOKUP.createException();
		}
		return attribute;
	}

	public String getLookupName() {
		return getFullNameSplit()[2];
	}
	
	public void parseValue(String value) {
		if (value!=null && !value.equals("")) {
			setValue(Integer.parseInt(value));
		}
	}
}
