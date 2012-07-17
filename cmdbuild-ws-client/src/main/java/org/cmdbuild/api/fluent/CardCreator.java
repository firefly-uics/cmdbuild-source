package org.cmdbuild.api.fluent;

/**
 * Card creator class.
 */
public interface CardCreator {

	/**
	 * Sets the className for the specified class.
	 * 
	 * @param className
	 *            the class name.
	 * 
	 * @return the {@link CardCreator} itself.
	 */
	CardCreator forClass(String className);

	/**
	 * Adds "Code" attribute.
	 * 
	 * @param value
	 *            the attribute's value.
	 * 
	 * @return the {@link CardCreator} itself.
	 */
	CardCreator withCode(String value);

	/**
	 * Adds "Description" attribute.
	 * 
	 * @param value
	 *            the attribute's value.
	 * 
	 * @return the {@link CardCreator} itself.
	 */
	CardCreator withDescription(String value);

	/**
	 * Adds an attribute.
	 * 
	 * @param name
	 *            the attribute's name.
	 * @param value
	 *            the attribute's value.
	 * 
	 * @return the {@link CardCreator} itself.
	 */
	CardCreator with(String name, String value);

	/**
	 * Adds an attribute.
	 * 
	 * @param name
	 *            the attribute's name.
	 * @param value
	 *            the attribute's value.
	 * 
	 * @return the {@link CardCreator} itself.
	 */
	CardCreator withAttribute(String name, String value);

	/**
	 * Creates the new card and returns it's descriptor.
	 * 
	 * @return the newly created card's descriptor.
	 */
	CardDescriptor create();

}