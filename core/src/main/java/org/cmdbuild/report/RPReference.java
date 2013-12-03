package org.cmdbuild.report;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.exception.ReportException.ReportExceptionType;

public class RPReference extends ReportParameter {

	public static class ReportReferenceAttributeType extends ReferenceAttributeType {

		private String referencedClassName;

		public ReportReferenceAttributeType(final CMDomain domain) {
			super(domain);
		}

		public ReportReferenceAttributeType(final String referencedClass) {
			super(UnsupportedProxyFactory.of(CMIdentifier.class).create());
			this.referencedClassName = referencedClass;
		}

		public String getReferencedClassName() {
			return this.referencedClassName;
		}

		@Override
		public void accept(final CMAttributeTypeVisitor visitor) {
			if (visitor instanceof CMAttributeTypeVisitorWithReportReference) {
				((CMAttributeTypeVisitorWithReportReference) visitor).visit(this);
			} else {
				visitor.visit(this);
			}
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

	public String getClassName() {
		return getFullNameSplit()[1];
	}

	public String getAttributeName() {
		return getFullNameSplit()[2];
	}

	@Override
	public void parseValue(final String value) {
		if (value != null && !value.equals("")) {
			setValue(Integer.parseInt(value));
		}
	}

	@Override
	public CMAttributeType<?> getCMAttributeType() {
		return new ReportReferenceAttributeType(getClassName());
	}

	public interface CMAttributeTypeVisitorWithReportReference extends CMAttributeTypeVisitor {
		void visit(ReportReferenceAttributeType attributeType);
	}
}
