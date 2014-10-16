package org.cmdbuild.service.rest.cxf.service;

import org.cmdbuild.service.rest.model.Session;

import com.google.common.base.Optional;

public interface TokenStore {

	void put(String token, Session credentials);

	Optional<Session> get(String token);

	void remove(String token);

}
