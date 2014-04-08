package org.cmdbuild.services.scheduler.reademail;

import org.cmdbuild.model.email.Email;

import com.google.common.base.Predicate;

/**
 * The rule that must be verified during the receiving process.
 */
public interface Rule extends Predicate<Email> {

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