package org.cmdbuild.common.mail;

import java.security.Security;

class DefaultMailApi implements MailApi {

	private final Configuration configuration;

	@SuppressWarnings("restriction")
	public DefaultMailApi(final Configuration configuration) {
		this.configuration = configuration;
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	}

	@Override
	public NewMail newMail() {
		return new DefaultNewMail(configuration);
	}

	@Override
	public SelectFolder selectFolder(final String folder) {
		return new DefaultSelectFolder(configuration, folder);
	}

	@Override
	public SelectMail selectMail(final FetchedMail mail) {
		return new DefaultSelectMail(configuration, mail);
	}

}
