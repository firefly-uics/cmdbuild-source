package org.cmdbuild.logic.data;

import org.cmdbuild.logic.Logic;

public interface LockLogic extends Logic {

	void lockCard(Long cardId);

	void unlockCard(Long cardId);

	void checkNotLockedCard(Long cardId);

	void checkCardLockedbyUser(Long cardId, String user);

	void lockActivity(Long instanceId, String activityId);

	void unlockActivity(Long instanceId, String activityId);

	void checkActivityLockedbyUser(Long instanceId, String activityId, String user);

	void checkNotLockedInstance(Long instanceId);

	void unlockAll();

}
