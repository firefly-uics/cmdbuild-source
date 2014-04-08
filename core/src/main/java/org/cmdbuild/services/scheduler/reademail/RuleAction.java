package org.cmdbuild.services.scheduler.reademail;

/**
 * The action associated with a specific {@link Rule}. Normally it's
 * returned by the {@link Rule.action(Email)} method.
 */
public interface RuleAction {

	/**
	 * Executes the action.
	 */
	void execute();

}