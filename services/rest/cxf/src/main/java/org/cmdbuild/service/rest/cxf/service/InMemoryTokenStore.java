package org.cmdbuild.service.rest.cxf.service;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.service.rest.model.Credentials;

import com.google.common.base.Optional;

public class InMemoryTokenStore implements TokenStore {

	private static final Optional<Credentials> ABSENT = Optional.absent();

	private final Map<String, Credentials> map;

	public InMemoryTokenStore() {
		map = newHashMap();
	}

	@Override
	public void put(final String token, final Credentials credentials) {
		Validate.notNull(token, "invalid token");
		Validate.notNull(credentials, "invalid credentials");
		map.put(token, credentials);
	}

	@Override
	public Optional<Credentials> get(final String token) {
		Validate.notNull(token, "invalid token");
		final Credentials credentials = map.get(token);
		return (credentials == null) ? ABSENT : Optional.of(credentials);
	}

	@Override
	public void remove(final String token) {
		Validate.notNull(token, "invalid token");
		map.remove(token);
	}

}
