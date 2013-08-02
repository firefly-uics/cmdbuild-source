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

	@Override
	public SelectFolder selectFolder(final String folder) {
		return new DefaultSelectFolder(configuration, folder);
	}

	@Override
	public SelectMail selectMail(final FetchedMail mail) {
		return new DefaultSelectMail(configuration, mail);
	}

}
