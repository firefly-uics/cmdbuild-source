package org.cmdbuild.workflow;

import javax.activation.DataSource;

import org.cmdbuild.workflow.xpdl.CMProcessDefinitionException;

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
	 * @throws CMProcessDefinitionException
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

}
