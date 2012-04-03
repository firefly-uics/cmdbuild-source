package org.cmdbuild.dao.legacywrappers;

import javax.activation.DataSource;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.xpdl.PackageHandler;
import org.cmdbuild.workflow.xpdl.XPDLException;

public class ProcessClassWrapper extends ClassWrapper implements CMProcessClass {

	private final PackageHandler packageHandler;

	public ProcessClassWrapper(final ITable table, final PackageHandler packageHandler) {
		super(table);
		this.packageHandler = packageHandler;
	}

	@Override
	public DataSource getDefinitionTemplate() throws XPDLException {
		return packageHandler.getXpdlTemplate(this);
	}

	@Override
	public boolean isUserStoppable() {
		return table.isUserStoppable();
	}
}
