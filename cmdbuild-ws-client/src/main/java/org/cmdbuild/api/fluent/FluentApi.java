package org.cmdbuild.api.fluent;

/**
 * API for access to CMDBuild data/schema.
 */
public interface FluentApi {

	/**
	 * Creates a new {@link CardCreator}.
	 * 
	 * @return a newly created {@link CardCreator}.
	 */
	CardCreator newCard();

}
