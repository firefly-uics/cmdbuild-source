package org.cmdbuild.workflow;

import java.util.List;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessDefInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;

public interface CMProcessInstance extends CMCard {

	// FIXME Unlucky name :(
	interface CMProcessInstanceDefinition extends CMCardDefinition {
		CMProcessInstanceDefinition set(String key, Object value);

		CMProcessInstanceDefinition setActivities(WSActivityInstInfo[] activityInfos) throws CMWorkflowException;
		void addActivity(WSActivityInstInfo activityInfo) throws CMWorkflowException;
		void removeActivity(String activityInstanceId) throws CMWorkflowException;

		CMProcessInstanceDefinition setState(WSProcessInstanceState state);

		/**
		 * Updates the 
		 * Used by service synchronization.
		 * 
		 * @param process definition information
		 * @return the {@link CMProcessInstanceDefinition} itself for chaining
		 */
		CMProcessInstanceDefinition setUniqueProcessDefinition(WSProcessDefInfo info);

		/**
		 * Save the process instance if something has changed
		 */
		CMProcessInstance save();
	}

	/**
	 * It should return {@link CMCard.getId()}. It is used to disambiguate
	 * between the card and process instance ids.
	 * 
	 * @return identifier of the data store card
	 */
	Object getCardId();

	/**
	 * We cannot override {@link CMCard.getId()} because it would break the
	 * method semantics.
	 * 
	 * @return identifier of the process instance
	 */
	String getProcessInstanceId();

	CMProcessClass getType();

	/**
	 * Get current process instance state.
	 * 
	 * @return the current process state
	 */
	WSProcessInstanceState getState();

	List<CMActivityInstance> getActivities();

	/**
	 * Returns an object with the ids to uniquely identify a process definition. 
	 * 
	 * @return unique process definition informations
	 */
	WSProcessDefInfo getUniqueProcessDefinition();
}
