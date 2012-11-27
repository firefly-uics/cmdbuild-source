package org.cmdbuild.elements.report;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

public class RPReference extends ReportParameter {

	protected RPReference(final JRParameter jrParameter) {
		super();
		setJrParameter(jrParameter);
		if (getJrParameter() == null || getFullName() == null || getFullName().equals("")
				|| !getFullName().matches(regExpLR))
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		if (getJrParameter().getValueClass() != Integer.class)
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_REFERENCE_CLASS.createException();

		// try to read table for class
		try {
			UserOperations.from(UserContext.systemContext()).tables().get(getClassName());
		} catch (final NotFoundException e) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_CMDBUILD_CLASS.createException();
		}

		// try to read attribute
		try {
			UserOperations.from(UserContext.systemContext()).tables().get(getClassName())
					.getAttribute(getAttributeName());
		} catch (final NotFoundException e) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_CMDBUILD_ATTRIBUTE.createException();
		}
	}

	@Override
	public IAttribute createCMDBuildAttribute(final ITableFactory tf) {
		final IAttribute attribute = createCMDBuildAttribute(tf, AttributeType.REFERENCE);

		// table
		final ITable table = tf.get(getClassName());

		// domain
		final IDomain domain = UserOperations.from(UserContext.systemContext()).domains().create();
		domain.setClass2(table);

		// attribute
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

	@Override
	public void parseValue(final String value) {
		if (value != null && !value.equals(""))
			setValue(Integer.parseInt(value));
	}

}
