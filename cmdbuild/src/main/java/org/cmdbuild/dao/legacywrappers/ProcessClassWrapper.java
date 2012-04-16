package org.cmdbuild.dao.legacywrappers;

import javax.activation.DataSource;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ProcessDefinitionManager;

public class ProcessClassWrapper extends ClassWrapper implements CMProcessClass {

	private final ProcessDefinitionManager processDefinitionManager;

	public ProcessClassWrapper(final ITable table, final ProcessDefinitionManager processDefinitionManager) {
		super(table);
		this.processDefinitionManager = processDefinitionManager;
	}

	@Override
	public DataSource getDefinitionTemplate() throws CMWorkflowException {
		return processDefinitionManager.getTemplate(this);
	}

	@Override
	public String[] getDefinitionVersions() throws CMWorkflowException {
		return processDefinitionManager.getVersions(this);
	}

	@Override
	public boolean isUserStoppable() {
		return table.isUserStoppable();
	}
}
