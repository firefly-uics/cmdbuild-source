package org.cmdbuild.services;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.springframework.beans.factory.annotation.Autowired;

public class CacheManager {

	@Autowired
	private CMBackend backend = CMBackend.INSTANCE;

	public void clearAllCaches() {
		clearDatabaseCache();
		TranslationService.getInstance().reload();
		JSONDispatcherService.getInstance().reload();
	}

	public void clearDatabaseCache() {
		backend.clearCache();
		TemporaryObjectsBeforeSpringDI.getDriver().clearCache();
	}
}
