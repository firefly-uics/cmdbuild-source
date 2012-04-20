package org.cmdbuild.workflow;

import javax.activation.DataSource;

/**
 * Handles all the operations on workflow definition files, including
 * template generation, download and upload from/to the workflow engine.
 */
public interface ProcessDefinitionManager {

	/**
	 * Returns a new process definition document for the process
	 * 
	 * @param process
	 * @return template document
	 * @throws CMWorkflowException
	 */
	DataSource getTemplate(CMProcessClass process) throws CMWorkflowException;

	/**
	 * Returns the process definition versions available in the repository.
	 * They can be in use or not.
	 * 
	 * @param process
	 * @return list of process definition versions
	 */
	String[] getVersions(CMProcessClass process) throws CMWorkflowException;

	/**
	 * Returns one version of the process definition document for the process.
	 * 
	 * @param process
	 * @param version
	 * @return document
	 * @throws CMWorkflowException
	 */
	DataSource getDefinition(CMProcessClass process, String version) throws CMWorkflowException;

	/**
	 * Associates a package definition to a process
	 * 
	 * @param process
	 * @param pkgDefData
	 * @throws CMWorkflowException
	 */
	void updateDefinition(CMProcessClass process, DataSource pkgDefData) throws CMWorkflowException;

}
