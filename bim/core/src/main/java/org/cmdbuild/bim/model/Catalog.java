package org.cmdbuild.bim.model;

import java.util.List;

public interface Catalog {

	/**
	 * @return the i-th entry of the catalog
	 * */
	EntityDefinition getEntityDefinition(int i);

	/**
	 * @return all the entries of the catalog
	 * */
	Iterable<EntityDefinition> getEntitiesDefinitions();

	/**
	 * @return print a summary of the catalog
	 * */
	void printSummary();

	/**
	 * @return the number of entries of the catalog
	 * */
	int getSize();

	boolean contains(String entityDefintionName);

	List<Integer> getPositionsOf(String entityDefintionName);

}
