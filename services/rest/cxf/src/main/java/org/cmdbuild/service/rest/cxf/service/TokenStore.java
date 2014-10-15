package org.cmdbuild.service.rest.cxf.service;

import org.cmdbuild.service.rest.model.Credentials;

import com.google.common.base.Optional;

public interface TokenStore {

	void put(String token, Credentials credentials);

	Optional<Credentials> get(String token);

	void remove(String token);

}
