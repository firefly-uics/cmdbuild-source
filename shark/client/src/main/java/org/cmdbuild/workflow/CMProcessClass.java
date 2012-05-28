package org.cmdbuild.workflow;

import javax.activation.DataSource;

import org.cmdbuild.dao.entrytype.CMClass;

/**
 * Class object extended for workflow handling
 */
public interface CMProcessClass extends CMClass {

	/**
	 * Creates a definition template for this process.  
	 * 
	 * @return a template process definition versions
	 * @throws CMWorkflowException
	 */
	DataSource getDefinitionTemplate() throws CMWorkflowException;

	/**
	 * Returns the available process definition versions .
	 * 
	 * @return process definition versions
	 * @throws CMWorkflowException
	 */
	String[] getDefinitionVersions() throws CMWorkflowException;

	/**
	 * Returns one version of the definition file for this process.  
	 * 
	 * @return process definition
	 * @throws CMWorkflowException
	 */
	DataSource getDefinition(String version) throws CMWorkflowException;

	/**
	 * Associates a package definition to this process
	 * 
	 * @param pkgDefData
	 * @throws CMWorkflowException
	 */
	void updateDefinition(DataSource pkgDefData) throws CMWorkflowException;

	/**
	 * Being stoppable by a user is a property of the process class. For some
	 * reason a few customers don't want this to be defined in the process
	 * workflow but they like this "hack" instead.
	 * 
	 * @return if process can be stopped by every user that can modify it
	 */
	boolean isUserStoppable();

	/**
	 * Returns the start activity for a group. It considers the latest
	 * package version that is bound to this process.
	 * 
	 * @param groupName
	 * @return start activity for the group
	 * @throws CMWorkflowException if no activity is defined for that group
	 */
	CMActivity getStartActivity(String groupName) throws CMWorkflowException;

}
