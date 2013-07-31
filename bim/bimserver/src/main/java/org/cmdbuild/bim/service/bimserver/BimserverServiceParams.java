package org.cmdbuild.bim.service.bimserver;

import org.cmdbuild.bim.service.ServiceParams;

public class BimserverServiceParams implements ServiceParams {

	private final String username;
	private final String password;

	public BimserverServiceParams(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

}
