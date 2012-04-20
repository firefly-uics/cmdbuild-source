package org.cmdbuild.workflow.xpdl;

import java.io.IOException;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.service.CMWorkflowService;

/**
 * Process Definition Manager that uses XPDL definitions.
 */
public abstract class AbstractProcessDefinitionManager implements ProcessDefinitionManager {

	private final CMWorkflowService workflowService;

	public AbstractProcessDefinitionManager(final CMWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@Override
	public final String[] getVersions(final CMProcessClass process) throws CMWorkflowException {
		return workflowService.getPackageVersions(getPackageId(process));
	}

	@Override
	public DataSource getDefinition(final CMProcessClass process, final String version) throws CMWorkflowException {
		final byte[] pkgDef = workflowService.downloadPackage(getPackageId(process), version);
		final ByteArrayDataSource ds = new ByteArrayDataSource(pkgDef, getMimeType());
		ds.setName(String.format("%s_%s.%s", process.getName(), version, getFileExtension()));
		return ds;
	}

	@Override
	public void updateDefinition(CMProcessClass process, DataSource pkgDefData) throws CMWorkflowException {
		try {
			byte[] binaryData = IOUtils.toByteArray(pkgDefData.getInputStream());
			workflowService.uploadPackage(getPackageId(process), binaryData);
		} catch (IOException e) {
			throw new CMWorkflowException(e);
		}		
	}

	protected abstract String getMimeType();
	protected abstract String getFileExtension();

	@Legacy("As in 1.x")
	protected final String getPackageId(final CMProcessClass process) {
		return "Package_" + process.getName().toLowerCase();
	}

	@Legacy("As in 1.x")
	protected final String getProcessId(final CMProcessClass process) {
		return "Process_" + process.getName().toLowerCase();
	}

}
