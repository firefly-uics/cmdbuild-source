package org.cmdbuild.common.mail;

class DefaultMailApi implements MailApi {

	private final Configuration configuration;

	public DefaultMailApi(final Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public NewMail newMail() {
		return new DefaultNewMail(configuration);
	}

}
