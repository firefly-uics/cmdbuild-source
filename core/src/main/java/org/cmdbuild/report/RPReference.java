package org.cmdbuild.report;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.exception.ReportException.ReportExceptionType;

public class RPReference extends ReportParameter {

	public static class ReportReferenceAttributeType extends ForeignKeyAttributeType {

		public ReportReferenceAttributeType(final String referencedClassName) {
			super(referencedClassName);
		}

	}

	protected RPReference(final JRParameter jrParameter) {
		super();
		setJrParameter(jrParameter);

		if (getJrParameter() == null //
				|| getFullName() == null //
				|| getFullName().equals("") //
				|| !getFullName().matches(regExpLR)) {

			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}

		if (getJrParameter().getValueClass() != Integer.class) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_REFERENCE_CLASS.createException();
		}
	}

	@Override
	public void accept(final ReportParameterVisitor visitor) {
		visitor.accept(this);
	}

	public String getClassName() {
		return getFullNameSplit()[1];
	}

	public String getAttributeName() {
		return getFullNameSplit()[2];
	}

	@Override
	public void parseValue(final Object value) {
		setValue(new IntegerAttributeType().convertValue(value));
	}

}
