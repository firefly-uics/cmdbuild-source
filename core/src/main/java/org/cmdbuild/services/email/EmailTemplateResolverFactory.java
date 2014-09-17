package org.cmdbuild.services.email;

import org.cmdbuild.services.email.EmailTemplateResolver.Configuration;

/**
 * {@link EmailTemplateResolver} factory.
 */
public interface EmailTemplateResolverFactory {

	/**
	 * Creates an instance of {@link EmailTemplateResolver}.
	 * 
	 * @return a new instance of {@link EmailTemplateResolver}.
	 */
	EmailTemplateResolver create();

	/**
	 * Creates an instance of {@link EmailTemplateResolver} based on the
	 * specified {@link Configuration}.
	 * 
	 * @param configuration
	 * 
	 * @return a new instance of {@link EmailTemplateResolver}.
	 */
	EmailTemplateResolver create(Configuration configuration);

}
