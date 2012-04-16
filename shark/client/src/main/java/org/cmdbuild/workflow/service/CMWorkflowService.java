package org.cmdbuild.workflow.service;

import org.cmdbuild.workflow.CMWorkflowException;

/**
 * Workflow service with low-level operations
 */
public interface CMWorkflowService {

	String[] getPackageVersions(String packageId) throws CMWorkflowException;

}
