package org.cmdbuild.elements.report;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.services.SchemaCache;

public class RPLookup extends ReportParameter {

	protected RPLookup(JRParameter jrParameter) {
		super();		
		setJrParameter(jrParameter);
		if(getJrParameter()==null || getFullName()==null || getFullName().equals("") || !getFullName().matches( regExpLR ))
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		if(getJrParameter().getValueClass() != Integer.class)
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_LOOKUP_CLASS.createException();
		
		//try to read lookup
		try {
			String lookupName = getLookupName();
			Iterable<Lookup> list = SchemaCache.getInstance().getLookupList(lookupName, null);
			if(!list.iterator().hasNext())
				throw NotFoundExceptionType.LOOKUP_NOTFOUND.createException(lookupName);
		} catch (Exception e) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_CMDBUILD_LOOKUP.createException();
		}
	}
	
	public IAttribute createCMDBuildAttribute(ITableFactory tf) {		
		IAttribute attribute = createCMDBuildAttribute(tf, AttributeType.LOOKUP);
		attribute.setLookupType(getLookupName());
		
		return attribute;
	}

	public String getLookupName() {
		return getFullNameSplit()[2];
	}
	
	public void parseValue(String value) {
		if(value!=null && !value.equals(""))
			setValue(Integer.parseInt(value));
	}
}
