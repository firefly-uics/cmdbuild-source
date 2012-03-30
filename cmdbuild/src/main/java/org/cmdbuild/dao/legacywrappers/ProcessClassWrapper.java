package org.cmdbuild.dao.legacywrappers;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IPAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.xpdl.XPDLDocument;
import org.cmdbuild.workflow.xpdl.XPDLException;
import org.cmdbuild.workflow.xpdl.XPDLDocument.ScriptLanguages;

public class ProcessClassWrapper extends ClassWrapper implements CMProcessClass {

	final ProcessType processType;

	public ProcessClassWrapper(final ProcessType processType) {
		super(processType);
		this.processType = processType;
	}

	@Override
	public XPDLDocument getXpdlTemplate() throws XPDLException {
		XPDLDocument doc = new XPDLDocument(getPackageId());
		doc.createCustomTypeDeclarations();
		doc.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		addProcessWithFields(doc);
		return doc;
	}

	private void addProcessWithFields(XPDLDocument doc) {
		final String wpId = getProcessId();
		doc.addProcess(wpId);
		addBindedClass(doc);
		final DaoToXpdlAttributeTypeConverter typeConverter = new DaoToXpdlAttributeTypeConverter();
		for (CMAttribute a : getAllAttributes()) {
			XPDLDocument.StandardAndCustomTypes type = typeConverter.convertType(a.getType());
			if (type != null) {
				doc.addProcessField(wpId, a.getName(), type);
			}
		}
	}

	@Legacy("As in 1.x")
	private void addBindedClass(XPDLDocument doc) {
		doc.addProcessExtendedAttribute(getProcessId(), "cmdbuildBindedClass", getName());
	}

	@Legacy("As in 1.x")
	private String getPackageId() {
		return "Package_" + getName().toLowerCase();
	}

	@Legacy("As in 1.x")
	private String getProcessId() {
		return "Process_" + getName().toLowerCase();
	}

	private class DaoToXpdlAttributeTypeConverter implements CMAttributeTypeVisitor {

		private XPDLDocument.StandardAndCustomTypes xpdlType;

		public XPDLDocument.StandardAndCustomTypes convertType(final CMAttributeType<?> type) {
			type.accept(this);
			return xpdlType;
		}

		public void visit(BooleanAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.BOOLEAN;
		}

		@Override
		public void visit(DateTimeAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.DATETIME;
		}

		@Override
		public void visit(DateAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.DATETIME;
		}

		@Override
		public void visit(DecimalAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.FLOAT;
		}

		@Override
		public void visit(DoubleAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.FLOAT;
		}

		@Override
		public void visit(ForeignKeyAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.REFERENCE;
		}

		@Override
		public void visit(GeometryAttributeType attributeType) {
			xpdlType = null;
		}

		@Override
		public void visit(IntegerAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.INTEGER;
		}

		@Override
		public void visit(IPAddressAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.STRING;
		}

		@Override
		public void visit(LookupAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.LOOKUP;
		}

		@Override
		public void visit(ReferenceAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.REFERENCE;
		}

		@Override
		public void visit(StringAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.STRING;
		}

		@Override
		public void visit(TextAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.STRING;
		}

		@Override
		public void visit(TimeAttributeType attributeType) {
			xpdlType = XPDLDocument.StandardAndCustomTypes.DATETIME;
		}

	}
}
