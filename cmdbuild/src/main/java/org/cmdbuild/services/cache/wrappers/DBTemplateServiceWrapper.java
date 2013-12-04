package org.cmdbuild.services.cache.wrappers;

import org.cmdbuild.services.DBTemplateService;
import org.cmdbuild.services.cache.CachingService.Cacheable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DBTemplateServiceWrapper implements Cacheable {

	private static final Marker marker = MarkerFactory.getMarker(DBTemplateServiceWrapper.class.getName());

	@Override
	public void clearCache() {
		logger.info(marker, "clearing templating service cache");
		new DBTemplateService().reload();
	}

}
