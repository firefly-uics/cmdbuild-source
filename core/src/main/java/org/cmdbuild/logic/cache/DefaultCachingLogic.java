package org.cmdbuild.logic.cache;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.services.cache.CachingService;
import org.cmdbuild.spring.annotations.LogicComponent;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@LogicComponent
public class DefaultCachingLogic implements CachingLogic, Logic {

	private static final Marker marker = MarkerFactory.getMarker(DefaultCachingLogic.class.getName());

	private final CachingService service;

	@Autowired
	public DefaultCachingLogic( //
			final CachingService service //
	) {
		this.service = service;
	}

	@Override
	public void clearCache() {
		logger.info(marker, "clearing cache");
		service.clearCache();
	}

}
