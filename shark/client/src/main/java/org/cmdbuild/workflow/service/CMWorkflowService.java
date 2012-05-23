package org.cmdbuild.workflow.service;

import org.cmdbuild.workflow.CMWorkflowException;

/**
 * Workflow service with low-level operations
 */
public interface CMWorkflowService {

	String[] getPackageVersions(String pkgId) throws CMWorkflowException;

	void uploadPackage(String pkgId, byte[] pkgDefData) throws CMWorkflowException;

	byte[] downloadPackage(String pkgId, String pkgVer) throws CMWorkflowException;

	/**
	 * Download all the open packages as byte arrays
	 * 
	 * @return an array of bytes one for each open package
	 * @throws CMWorkflowException 
	 */
	byte[][] downloadAllPackages() throws CMWorkflowException;
}
