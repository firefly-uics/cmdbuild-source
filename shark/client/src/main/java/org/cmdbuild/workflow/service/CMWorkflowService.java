package org.cmdbuild.workflow.service;

import java.util.Map;

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

	/**
	 * Create and start the process.
	 *  
	 * @param pkgId package id
	 * @param procDefId workflow process definition id (as defined in the xpdl)
	 * @return newly created process instance id
	 * @throws CMWorkflowException
	 */
	String startProcess(String pkgId, String procDefId) throws CMWorkflowException;

	void setProcessInstanceVariables(String procInstId, Map<String, Object> variables) throws CMWorkflowException;

	Map<String, Object> getProcessInstanceVariables(String procInstId) throws CMWorkflowException;

	/**
	 * Returns a list of open activities for a process instance.
	 * 
	 * @param procInstId
	 * @return list of open activity instances for the process instance
	 * @throws CMWorkflowException
	 */
	WSActivityInstInfo[] findOpenActivitiesForProcessInstance(String procInstId) throws CMWorkflowException;

	/**
	 * Returns a list of open activities for a process definition.
	 * 
	 * @param procDefId
	 * @return list of open activity instances for the process definition
	 * @throws CMWorkflowException
	 */
	WSActivityInstInfo[] findOpenActivitiesForProcess(String procDefId) throws CMWorkflowException;

	/**
	 * Aborts the current activity, stopping that flow path.
	 * 
	 * @param procInstId process instance id
	 * @param actInstId activity instance id
	 * @throws CMWorkflowException
	 */
	void abortActivityInstance(String procInstId, String actInstId) throws CMWorkflowException;
}
