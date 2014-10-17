package org.cmdbuild.service.rest.cxf.service;

import org.cmdbuild.service.rest.model.Session;

import com.google.common.base.Optional;

public interface SessionStore {

	void put(Session element);

	Optional<Session> get(String id);

	void remove(String id);

}
