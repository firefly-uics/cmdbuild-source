package org.cmdbuild.workflow;

import javax.activation.DataSource;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.workflow.xpdl.ProcessDefinitionException;

/**
 * Class object extended for workflow handling
 */
public interface CMProcessClass extends CMClass {

	/**
	 * Creates a definition template for this process.  
	 * 
	 * @return a template process definition versions
	 */
	DataSource getDefinitionTemplate() throws ProcessDefinitionException;

	/**
	 * Being stoppable by a user is a property of the process class. For some
	 * reason a few customers don't want this to be defined in the process
	 * workflow but they like this "hack" instead.
	 * 
	 * @return if process can be stopped by every user that can modify it
	 */
	boolean isUserStoppable();
}
