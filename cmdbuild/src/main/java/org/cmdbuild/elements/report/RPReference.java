package org.cmdbuild.elements.report;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.DataAccessLogic;

public class RPReference extends ReportParameter {


	final CMClass theClass;
	final CMAttribute theAttribute;

	protected RPReference(final JRParameter jrParameter) {
		super();
		setJrParameter(jrParameter);

		if (getJrParameter() == null 
				|| getFullName() == null
				|| getFullName().equals("")
				|| !getFullName().matches(regExpLR)) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}

		if (getJrParameter().getValueClass() != Integer.class) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_REFERENCE_CLASS.createException();
		}

		// try to read table for class
		try {
			DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
			theClass = dataAccessLogic.findClass(getClassName());
			if (theClass == null) {
				throw ReportExceptionType.REPORT_INVALID_PARAMETER_CMDBUILD_CLASS.createException();
			}
		} catch (final NotFoundException e) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_CMDBUILD_CLASS.createException();
		}

		// try to read attribute
		try {
			theAttribute = theClass.getAttribute(getAttributeName());
		} catch (final NotFoundException e) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_CMDBUILD_ATTRIBUTE.createException();
		}
	}

	public String getClassName() {
		return getFullNameSplit()[1];
	}

	public String getAttributeName() {
		return getFullNameSplit()[2];
	}

	@Override
	public void parseValue(final String value) {
		if (value != null && !value.equals(""))
			setValue(Integer.parseInt(value));
	}

	@Override
	public CMAttribute createCMDBuildAttribute() {
		return theAttribute;
	}

}
