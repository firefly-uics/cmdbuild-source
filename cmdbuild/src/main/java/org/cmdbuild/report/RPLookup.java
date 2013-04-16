package org.cmdbuild.report;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.exception.ReportException.ReportExceptionType;

public class RPLookup extends ReportParameter {

	protected RPLookup(JRParameter jrParameter) {
		super();
		setJrParameter(jrParameter);
		if (getJrParameter() == null 
				|| getFullName() == null
				|| getFullName().equals("")
				|| !getFullName().matches(regExpLR)) {

			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}

		if (getJrParameter().getValueClass() != Integer.class) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_LOOKUP_CLASS.createException();
		}
	}

	public String getLookupName() {
		return getFullNameSplit()[2];
	}

	public void parseValue(String value) {
		if (value!=null && !value.equals("")) {
			setValue(Integer.parseInt(value));
		}
	}

	@Override
	public CMAttribute createCMDBuildAttribute() {

		// retrieve the LookupType via Store,
		// using the getLookupName to have the
		// lookup type name
		// throw ReportExceptionType.REPORT_INVALID_PARAMETER_CMDBUILD_LOOKUP.createException();
		// if the lookup is not retrieved
		throw new UnsupportedOperationException();

	}
}
