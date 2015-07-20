package org.cmdbuild.logic.data;

import org.cmdbuild.logic.Logic;

public interface LockLogic extends Logic {

	void lockCard(Long cardId);

	void unlockCard(Long cardId);

	void unlockAllCards();

	void lockActivity(Long instanceId, String activityId);

	void unlockActivity(Long instanceId, String activityId);

	void unlockAllActivities();

}
