package org.cmdbuild.services.scheduler.reademail;

/**
 * Generic {@link Rule} factory.
 */
public interface RuleFactory<T extends Rule> {

	/**
	 * Creates the specific {@link Rule}.
	 * 
	 * @return the created specific {@link Rule}.
	 */
	public T create();

}
