package org.cmdbuild.auth;

import static com.google.common.collect.HashBiMap.create;

import org.cmdbuild.auth.user.OperationUser;

import com.google.common.collect.BiMap;

public class DefaultTokenManager implements TokenManager {

	private final TokenGenerator tokenGenerator;
	private final BiMap<OperationUser, String> map;

	public DefaultTokenManager(final TokenGenerator tokenGenerator) {
		this.tokenGenerator = tokenGenerator;
		this.map = create();
	}

	@Override
	public void settingUser(final OperationUser value) {
		if (value != null) {
			map.put(value, tokenGenerator.generate(value.getAuthenticatedUser().getUsername()));
		}
	}

	@Override
	public String getToken(final OperationUser value) {
		return map.get(value);
	}

	@Override
	public OperationUser getUser(final String value) {
		return map.inverse().get(value);
	}

}
