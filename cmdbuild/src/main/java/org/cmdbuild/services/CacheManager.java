package org.cmdbuild.services;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.dao.backend.CMBackend;
import org.springframework.beans.factory.annotation.Autowired;

@OldDao
public class CacheManager {

	@Autowired
	private CMBackend backend = CMBackend.INSTANCE;

	public void clearAllCaches() {
		clearDatabaseCache();
		TranslationService.getInstance().reload();
		JSONDispatcherService.getInstance().reload();
	}

	//TODO: delete the line with "backend.clearCache"
	@OldDao
	@Deprecated
	public void clearDatabaseCache() {
		backend.clearCache();
	}
}
