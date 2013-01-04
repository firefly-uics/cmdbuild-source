package org.cmdbuild.logic.mappers;

public interface FilterValidator {

	/**
	 * It validates a filter
	 * @throws IllegalArgumentException if the filter is not valid
	 */
	void validate() throws IllegalArgumentException;
	
}
