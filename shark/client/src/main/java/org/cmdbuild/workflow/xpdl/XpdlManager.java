package org.cmdbuild.workflow.xpdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IPAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguages;

/**
 * Process Definition Manager that uses XPDL definitions.
 */
public class XpdlManager extends AbstractProcessDefinitionManager {

	public interface GroupQueryAdapter {

		String[] getAllGroupNames();
	}

	private static final String DEFAULT_SYSTEM_PARTICIPANT = "System";

	final GroupQueryAdapter groupQueryAdapter;


	public XpdlManager(final CMWorkflowService workflowService, final GroupQueryAdapter groupQueryAdapter) {
		super(workflowService);
		this.groupQueryAdapter = groupQueryAdapter;
	}

	@Override
	public DataSource getTemplate(final CMProcessClass process) throws XpdlException {
		XpdlDocument doc = new XpdlDocument(getPackageId(process));
		doc.createCustomTypeDeclarations();
		doc.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		addProcessWithFields(doc, process);
		doc.addSystemParticipant(DEFAULT_SYSTEM_PARTICIPANT);
		addAllGroupsToTemplate(doc);
		// Applications NOT NEEDED?
		return createDataSource(process, doc);
	}

	private DataSource createDataSource(final CMProcessClass process, final XpdlDocument doc) throws XpdlException {
		final byte[] xpdl = XpdlPackageFactory.xpdlByteArray(doc.getPkg());
		final ByteArrayDataSource ds = new ByteArrayDataSource(xpdl, getMimeType());
		ds.setName(String.format("%s.%s", process.getName(), getFileExtension()));
		return ds;
	}

	@Legacy("Should use the new authentication framework, passed as a constructor parameter")
	private void addAllGroupsToTemplate(XpdlDocument doc) {
		for (String name : groupQueryAdapter.getAllGroupNames()) {
			doc.addRoleParticipant(name);
		}
	}

	private void addProcessWithFields(XpdlDocument doc, final CMProcessClass process) {
		final String wpId = getProcessId(process);
		XpdlProcess proc = doc.createProcess(wpId);
		addBindedClass(doc, process);
		final DaoToXpdlAttributeTypeConverter typeConverter = new DaoToXpdlAttributeTypeConverter();
		for (CMAttribute a : process.getAllAttributes()) {
			XpdlDocument.StandardAndCustomTypes type = typeConverter.convertType(a.getType());
			if (type != null) {
				proc.addField(a.getName(), type);
			}
		}
	}

	@Legacy("As in 1.x")
	private void addBindedClass(XpdlDocument doc, final CMProcessClass process) {
		doc.findProcess(getProcessId(process)).setBindToClass(process.getName());
	}

	@Override
	public void updateDefinition(CMProcessClass process, DataSource pkgDefData) throws CMWorkflowException {
		try {
			final XpdlDocument xpdl = new XpdlDocument(XpdlPackageFactory.readXpdl(pkgDefData.getInputStream()));
			if (!getPackageId(process).equals(xpdl.getPackageId())) {
				throw new XpdlException("The package id does not match");
			}
			final XpdlProcess proc = xpdl.findProcess(getProcessId(process));
			if (proc == null) {
				throw new XpdlException("The process id does not match");
			}
			final String bindedClass = proc.getBindToClass();
			if (!process.getName().equals(bindedClass)) {
				throw new XpdlException("The process is not bound to this class");
			}
		} catch (IOException e) {
			throw new CMWorkflowException(e);
		}
		super.updateDefinition(process, pkgDefData);
	}

	@Override
	protected void addPackage(byte[] pkgDef, Map<String, ProcessInfo> processInfoMap) {
		try {
			final XpdlDocument xpdl = new XpdlDocument(XpdlPackageFactory.readXpdl(pkgDef));
			for (XpdlProcess xproc : xpdl.findAllProcesses()) {
				final String className = xproc.getBindToClass();
				if (className != null) {
					processInfoMap.put(className, createProcessInfo(xproc));
				}
			}
		} catch (XpdlException e) {
			// TODO LOG failure
		}
	}

	private ProcessInfo createProcessInfo(final XpdlProcess xproc) {
		ProcessInfo info = new ProcessInfo();
		info.packageId = xproc.getDocument().getPackageId();
		info.startActivities = new ArrayList<CMActivity>();
		for (final XpdlActivity xact : xproc.getStartingActivities()) {
			info.startActivities.add(new XpdlActivityWrapper(xact));
		}
		return info;
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

		private XpdlDocument.StandardAndCustomTypes xpdlType;

		public XpdlDocument.StandardAndCustomTypes convertType(final CMAttributeType<?> type) {
			type.accept(this);
			return xpdlType;
		}

		public void visit(BooleanAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.BOOLEAN;
		}

		@Override
		public void visit(DateTimeAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.DATETIME;
		}

		@Override
		public void visit(DateAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.DATETIME;
		}

		@Override
		public void visit(DecimalAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.FLOAT;
		}

		@Override
		public void visit(DoubleAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.FLOAT;
		}

		@Override
		public void visit(ForeignKeyAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.REFERENCE;
		}

		@Override
		public void visit(GeometryAttributeType attributeType) {
			xpdlType = null;
		}

		@Override
		public void visit(IntegerAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.INTEGER;
		}

		@Override
		public void visit(IPAddressAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.STRING;
		}

		@Override
		public void visit(LookupAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.LOOKUP;
		}

		@Override
		public void visit(ReferenceAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.REFERENCE;
		}

		@Override
		public void visit(StringAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.STRING;
		}

		@Override
		public void visit(TextAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.STRING;
		}

		@Override
		public void visit(TimeAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.DATETIME;
		}
	}

}
