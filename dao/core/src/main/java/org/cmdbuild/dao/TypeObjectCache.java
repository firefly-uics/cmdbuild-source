package org.cmdbuild.dao;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBClass;

public interface TypeObjectCache {

	/**
	 * Adds an object to the cache.
	 * 
	 * @param typeObject
	 *            the object to be added.
	 */
	void add(CMTypeObject typeObject);

	/**
	 * Removes an object from the cache.
	 * 
	 * @param typeObject
	 *            the object to be removed.
	 */
	void remove(CMTypeObject typeObject);

	/**
	 * Fetches the specified id from specific object type.
	 * 
	 * @param typeObjectClass
	 *            the object class.
	 * @param id
	 *            the id that needs to be fetched.
	 * 
	 * @return the fetched object or {@code null} if the object is not found.
	 */
	<T extends CMTypeObject> T fetch(Class<? extends CMTypeObject> typeObjectClass, Long id);

	/**
	 * Fetches the specified identifier from specific object type.
	 * 
	 * @param typeObjectClass
	 *            the object class.
	 * @param identifier
	 *            the identifier that needs to be fetched.
	 * 
	 * @return the fetched object or {@code null} if the object is not found.
	 */
	<T extends CMTypeObject> T fetch(Class<? extends CMTypeObject> typeObjectClass, CMIdentifier identifier);

	/**
	 * @return true if the cache for the classes is empty, false otherwise.
	 */
	public boolean hasNoClass();

	/**
	 * Clears the whole cache of the driver.
	 */
	void clearCache();

	/**
	 * Clears only classes from cache.
	 */
	void clearClasses();

	/**
	 * Clears only domains from cache.
	 */
	void clearDomains();

	/**
	 * Clears only functions from cache.
	 */
	void clearFunctions();

	List<DBClass> fetchCachedClasses();

}
