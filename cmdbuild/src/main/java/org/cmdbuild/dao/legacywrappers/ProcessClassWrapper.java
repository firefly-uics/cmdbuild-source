package org.cmdbuild.dao.legacywrappers;

import javax.activation.DataSource;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ProcessDefinitionManager;

public class ProcessClassWrapper extends ClassWrapper implements CMProcessClass {

	private final UserContext userCtx;
	private final ProcessDefinitionManager processDefinitionManager;

	public ProcessClassWrapper(final UserContext userCtx, final ITable table, final ProcessDefinitionManager processDefinitionManager) {
		super(table);
		this.userCtx = userCtx;
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
	public DataSource getDefinition(final String version) throws CMWorkflowException {
		return processDefinitionManager.getDefinition(this, version);
	}

	@Override
	public void updateDefinition(DataSource pkgDefData) throws CMWorkflowException {
		processDefinitionManager.updateDefinition(this, pkgDefData);
	}

	@Override
	public boolean isUserStoppable() {
		return table.isUserStoppable();
	}

	@Override
	public CMActivity getStartActivity() throws CMWorkflowException {
		final String groupName;
		if (userCtx.privileges().isAdmin()) {
			groupName = null;
		} else {
			groupName = userCtx.getWFStartGroup().getName();
		}
		return processDefinitionManager.getStartActivity(this, groupName);
	}

	@Override
	public String getPackageId() throws CMWorkflowException {
		return processDefinitionManager.getPackageId(this);
	}

	@Override
	public String getProcessDefinitionId() throws CMWorkflowException {
		return processDefinitionManager.getProcessDefinitionId(this);
	}

}
