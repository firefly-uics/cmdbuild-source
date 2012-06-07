package org.cmdbuild.workflow;

import java.util.List;

import org.cmdbuild.dao.entry.CMCard;

public interface CMProcessInstance extends CMCard {

	CMProcessClass getType();
	List<CMActivityInstance> getActivities();
}
