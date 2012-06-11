package org.cmdbuild.workflow;

import java.util.List;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.workflow.service.WSActivityInstInfo;

public interface CMProcessInstance extends CMCard {

	// FIXME Unlucky name :(
	interface CMProcessInstanceDefinition extends CMCardDefinition {
		CMProcessInstanceDefinition set(String key, Object value);
		void addActivity(WSActivityInstInfo activityInfo);
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

	List<CMActivityInstance> getActivities();
}
