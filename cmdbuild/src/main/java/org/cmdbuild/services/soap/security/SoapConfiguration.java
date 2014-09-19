package org.cmdbuild.services.soap.security;

import java.util.Collection;

import org.cmdbuild.auth.DefaultAuthenticationService.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

public class SoapConfiguration implements Configuration {

	private final Configuration configuration;

	@Autowired
	public SoapConfiguration( //
			final Configuration configuration //
	) {
		this.configuration = configuration;
	}

	@Override
	public Collection<String> getActiveAuthenticators() {
		return Sets.newHashSet(SoapPasswordAuthenticator.class.getSimpleName());
	}

	@Override
	public Collection<String> getServiceUsers() {
		return configuration.getServiceUsers();
	}

	@Override
	public Collection<String> getPrivilegedServiceUsers() {
		return configuration.getPrivilegedServiceUsers();
	}

}
