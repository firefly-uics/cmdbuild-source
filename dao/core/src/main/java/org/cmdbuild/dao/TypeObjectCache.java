package org.cmdbuild.dao;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.DBClass;

public interface TypeObjectCache {
	/**
	 * Adds a CMTypeObject to the cache
	 */
	void add(CMTypeObject typeObject);

	/**
	 * Removes the CMTypeObject from the cache
	 */
	void remove(CMTypeObject typeObject);

	/**
	 * Returns true if the cache contains the object of class = clazz with id =
	 * id
	 * 
	 * @param clazz
	 *            the class object of the CMTypeObject
	 * @param id
	 *            the id of the CMTypeObject
	 * @return
	 */
	<T extends CMTypeObject> T fetch(Class<? extends CMTypeObject> typeObjectClass, Long id);

	/**
	 * Returns true if the cache contains the object of class = clazz with name
	 * = name
	 * 
	 * @param clazz
	 *            the class object of the CMTypeObject
	 * @param name
	 *            the name of the CMTypeObject
	 * @return
	 */
	<T extends CMTypeObject> T fetch(Class<? extends CMTypeObject> typeObjectClass, String name);
	
	/**
	 * 
	 * @return true if the cache for the classes is empty, false otherwise
	 */
	public boolean hasNoClass();

	/**
	 * Clears the whole cache of the driver
	 */
	void clearCache();

	/**
	 * Clears only classes from cache
	 */
	void clearClasses();

	/**
	 * Clears only domains from cache
	 */
	void clearDomains();

	/**
	 * Clears only functions from cache
	 */
	void clearFunctions();

	List<DBClass> fetchCachedClasses();
}
