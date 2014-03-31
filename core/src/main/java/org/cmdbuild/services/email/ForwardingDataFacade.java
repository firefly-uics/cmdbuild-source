package org.cmdbuild.services.email;

import org.cmdbuild.services.email.EmailTemplateResolver.DataFacade;

public abstract class ForwardingDataFacade implements DataFacade {

	private final DataFacade dataFacade;

	protected ForwardingDataFacade(final DataFacade dataFacade) {
		this.dataFacade = dataFacade;
	}

	@Override
	public String getEmailForUser(final String user) {
		return dataFacade.getEmailForUser(user);
	}

	@Override
	public String getEmailForGroup(final String group) {
		return dataFacade.getEmailForGroup(group);
	}

	@Override
	public Iterable<String> getEmailsForGroupUsers(final String group) {
		return dataFacade.getEmailsForGroupUsers(group);
	}

	@Override
	public String getAttributeValue(final String attribute) {
		return dataFacade.getAttributeValue(attribute);
	}

	@Override
	public String getReferenceAttributeValue(final String attribute, final String subAttribute) {
		return dataFacade.getReferenceAttributeValue(attribute, subAttribute);
	}

}
