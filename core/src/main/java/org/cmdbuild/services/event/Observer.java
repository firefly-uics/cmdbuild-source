package org.cmdbuild.services.event;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

public interface Observer {

	Logger logger = Log.PERSISTENCE;

	void afterCreate(CMCard card);

	void beforeUpdate(CMCard actual, CMCard next);

	void afterUpdate(CMCard previous, CMCard actual);

	void beforeDelete(CMCard card);

}