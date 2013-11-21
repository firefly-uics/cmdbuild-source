package org.cmdbuild.services.email;

import org.cmdbuild.model.email.Email;

/**
 * Resolves text according with the following rules:<br>
 * <br>
 * <ul>
 * <li>"{user:foo}": all the e-mail addresses of the user {@code foo}</li>
 * <li>"{group:foo}": all the e-mail addresses of the group {@code foo}</li>
 * <li>"{groupUsers:foo}": all the e-mail addresses of the users of the group
 * {@code foo}</li>
 * <li>"{attribute:foo}": the value of the attribute named {@code foo}</li>
 * <li>"{attribute:foo:bar}": the value of the attribute named {@code bar} of
 * the card referenced by the attribute {@code foo}</li>
 * <li>the template as is otherwise</li>
 * </ul>
 */
public abstract class EmailTemplateResolver {

	public interface Configuration {

		DataFacade dataFacade();

		String multiSeparator();

	}

	public interface DataFacade {

		String getEmailForUser(String user);

		String getEmailForGroup(String group);

		Iterable<String> getEmailsForGroupUsers(String group);

		String getAttributeValue(String attribute);

		String getReferenceAttributeValue(String attribute, String subAttribute);

	}

	private final Configuration configuration;

	public EmailTemplateResolver(final Configuration configuration) {
		this.configuration = configuration;
	}

	protected DataFacade dataFacade() {
		return configuration.dataFacade();
	}

	protected String multiSeparator() {
		return configuration.multiSeparator();
	}

	// public abstract String resolve(String template);

	public abstract String resolve(String template, Email email);

}
