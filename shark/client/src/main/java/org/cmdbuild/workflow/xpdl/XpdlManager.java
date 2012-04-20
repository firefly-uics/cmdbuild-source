package org.cmdbuild.workflow.xpdl;

import java.io.IOException;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IPAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.xpdl.XPDLDocument.ScriptLanguages;

/**
 * Process Definition Manager that uses XPDL definitions.
 */
public class XpdlManager extends AbstractProcessDefinitionManager {

	public interface GroupQueryAdapter {

		String[] getAllGroupNames();
	}

	private static final String DEFAULT_SYSTEM_PARTICIPANT = "System";
	@Legacy("As in 1.x") private static final String CMDBUILD_BINDED_CLASS = "cmdbuildBindedClass";

	final GroupQueryAdapter groupQueryAdapter;


	public XpdlManager(final CMWorkflowService workflowService, final GroupQueryAdapter groupQueryAdapter) {
		super(workflowService);
		this.groupQueryAdapter = groupQueryAdapter;
	}

	@Override
	public DataSource getTemplate(final CMProcessClass process) throws XPDLException {
		XPDLDocument doc = new XPDLDocument(getPackageId(process));
		doc.createCustomTypeDeclarations();
		doc.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		addProcessWithFields(doc, process);
		doc.addSystemParticipant(DEFAULT_SYSTEM_PARTICIPANT);
		addAllGroupsToTemplate(doc);
		// Extended attribute on process userStoppable NOT NEEDED
		// Applications NOT NEEDED?
		return createDataSource(process, doc);
	}

	private DataSource createDataSource(final CMProcessClass process, final XPDLDocument doc) throws XPDLException {
		final byte[] xpdl = XPDLPackageFactory.xpdlByteArray(doc.getPkg());
		final ByteArrayDataSource ds = new ByteArrayDataSource(xpdl, getMimeType());
		ds.setName(String.format("%s.%s", process.getName(), getFileExtension()));
		return ds;
	}

	@Legacy("Should use the new authentication framework, passed as a constructor parameter")
	private void addAllGroupsToTemplate(XPDLDocument doc) {
		for (String name : groupQueryAdapter.getAllGroupNames()) {
			doc.addRoleParticipant(name);
		}
	}

	private void addProcessWithFields(XPDLDocument doc, final CMProcessClass process) {
		final String wpId = getProcessId(process);
		doc.addProcess(wpId);
		addBindedClass(doc, process);
		final DaoToXpdlAttributeTypeConverter typeConverter = new DaoToXpdlAttributeTypeConverter();
		for (CMAttribute a : process.getAllAttributes()) {
			XPDLDocument.StandardAndCustomTypes type = typeConverter.convertType(a.getType());
			if (type != null) {
				doc.addProcessField(wpId, a.getName(), type);
			}
		}
	}

	@Legacy("As in 1.x")
	private void addBindedClass(XPDLDocument doc, final CMProcessClass process) {
		doc.addProcessExtendedAttribute(getProcessId(process), CMDBUILD_BINDED_CLASS, process.getName());
	}

	@Override
	public void updateDefinition(CMProcessClass process, DataSource pkgDefData) throws CMWorkflowException {
		try {
			final XPDLDocument xpdl = new XPDLDocument(XPDLPackageFactory.readXpdl(pkgDefData.getInputStream()));
			if (!getPackageId(process).equals(xpdl.getPackageId())) {
				throw new XPDLException("The package id does not match");
			}
			final String bindedClass = xpdl.getProcessExtendedAttribute(getProcessId(process), CMDBUILD_BINDED_CLASS);
			if (!process.getName().equals(bindedClass)) {
				throw new XPDLException("The process id does not match");
			}
		} catch (IOException e) {
			throw new CMWorkflowException(e);
		}
		super.updateDefinition(process, pkgDefData);
	}

	@Override
	protected String getMimeType() {
		return "application/x-xpdl";
	}

	@Override
	protected String getFileExtension() {
		return "xpdl";
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
		public void visit(EntryTypeAttributeType attributeType) {
			xpdlType = null;
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
