package org.cmdbuild.logic.validators;

/**
 * Interface that can be implemented by each class whose purpose is to validate something 
 */
public interface Validator {

	/**
	 * If validation is ok, it returns nothing. If the validation fails, it
	 * throws an IllegalArgumentException.
	 * 
	 * @throws IllegalArgumentException
	 */
	void validate() throws IllegalArgumentException;

}
