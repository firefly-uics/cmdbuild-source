package org.cmdbuild.logic.email.rules;

import org.cmdbuild.services.email.EmailCallbackHandler;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;

/**
 * Generic {@link EmailCallbackHandler.Rule} factory.
 */
public interface RuleFactory<T extends Rule> {

	/**
	 * Creates the specific {@link EmailCallbackHandler.Rule}.
	 * 
	 * @return the created specific {@link EmailCallbackHandler.Rule}.
	 */
	public T create();

}
