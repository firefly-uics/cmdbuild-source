package org.cmdbuild.services.email;

import org.cmdbuild.model.email.Email;

/**
 * Handler for {@link Email} reception.
 */
public interface EmailCallbackHandler {

	interface Applicable {

		/**
		 * Checks if the {@link Rule} can be applied to the specified
		 * {@link Email}.
		 * 
		 * @param email
		 * 
		 * @return {@code true} if can be applied, {@code false} otherwise.
		 */
		boolean applies(Email email);

	}

	/**
	 * The rule that must be verified during the receiving process.
	 */
	interface Rule extends Applicable {

		/**
		 * Adapts the specified {@link Email} (if needed).
		 * 
		 * @param email
		 * 
		 * @return the adapted {@link Email}.
		 */
		Email adapt(Email email);

		/**
		 * Gets the {@link RuleAction} associated with this {@link Rule}.
		 * 
		 * @param email
		 *            is the currently received {@link Email}.
		 * 
		 * @return the {@link RuleAction} with this {@link Rule}.
		 */
		RuleAction action(Email email);

	}

	/**
	 * The action associated with a specific {@link Rule}. Normally it's
	 * returned by the {@link Rule.action(Email)} method.
	 */
	interface RuleAction {

		/**
		 * Executes the action.
		 */
		void execute();

	}

	/**
	 * Gets all the {@link Rule}s that can be applied to the receiving process.
	 * 
	 * @return the {@link Rule}s that must be verified during the receiving
	 *         process.
	 */
	Iterable<? extends Rule> getRules();

	/**
	 * Notifies the caller that a {@link RuleAction} can be executed.
	 * 
	 * @param action
	 *            the {@link RuleAction} that can be executed.
	 */
	void notify(RuleAction action);

}