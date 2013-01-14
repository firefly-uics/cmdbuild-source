package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMDomain.CMDomainDefinition;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.QuerySpecsBuilder;

/**
 * This interface provides an abstract view over the data model.
 */
public interface CMDataView {

	CMClass findClassById(Long id);

	CMClass findClassByName(String name);

	/**
	 * Returns the active classes.
	 * 
	 * @return active classes
	 */
	Iterable<? extends CMClass> findClasses();

	/**
	 * Returns all (active and inactive) classes.
	 * 
	 * @return all classes (active and inactive)
	 */
	Iterable<? extends CMClass> findAllClasses();

	CMClass createClass(CMClassDefinition definition);

	CMClass updateClass(CMClassDefinition definition);

	void deleteClass(CMClass cmClass);

	CMAttribute createAttribute(CMAttributeDefinition definition);

	CMAttribute updateAttribute(CMAttributeDefinition definition);

	void deleteAttribute(CMAttribute attribute);

	CMDomain findDomainById(Long id);

	CMDomain findDomainByName(String name);

	/**
	 * Returns the active domains.
	 * 
	 * @return active domains
	 */
	Iterable<? extends CMDomain> findDomains();

	/**
	 * Returns the active domains for a class
	 * 
	 * @param type
	 *            the class i'm requesting the domains for
	 * 
	 * @return active domains for that class
	 */
	Iterable<? extends CMDomain> findDomainsFor(CMClass type);

	/**
	 * Returns all (active and inactive) domains. It should be used by the
	 * Database Designer only.
	 * 
	 * @return all domains (active and inactive)
	 */
	Iterable<? extends CMDomain> findAllDomains();

	CMDomain createDomain(CMDomainDefinition definition);

	CMDomain updateDomain(CMDomainDefinition definition);

	void deleteDomain(CMDomain domain);

	CMFunction findFunctionByName(String name);

	/**
	 * Returns the functions defined in the schema.
	 * 
	 * @return defined functions
	 */
	Iterable<? extends CMFunction> findAllFunctions();

	/**
	 * Returns an empty card to be modified and saved.
	 * 
	 * Note: it does not create a card in the data store until
	 * {@link CMCardDefinition#save()} is called on the resulting object.
	 * 
	 * @param type
	 *            class for the card
	 * 
	 * @return an empty modifiable card
	 */
	CMCardDefinition newCard(CMClass type);

	/**
	 * Returns a modifiable card.
	 * 
	 * Note: the changes are not saved in the data store until
	 * {@link CMCardDefinition#save()} is called on the resulting object.
	 * 
	 * @param card
	 *            immutable card to be modified
	 * 
	 * @return a modifiable card from the immutable card
	 */
	CMCardDefinition modifyCard(CMCard card);

	/**
	 * Method that returns a mutable relation object. This object is a new
	 * relation which will be created and stored in the database
	 * 
	 * @param domain
	 *            the domain which the relation will belong to
	 * @return a mutable object
	 */
	CMRelationDefinition newRelation(CMDomain domain);

	/**
	 * Method that returns a mutable relation object. This object is an object
	 * representing a relation which already exists in the database
	 * 
	 * @param domain
	 *            the domain which the relation belongs to
	 * @return a mutable object
	 */
	CMRelationDefinition modifyRelation(CMRelation relation);

	/**
	 * Starts a query. Invoke {@link QuerySpecsBuilder.run()} to execute it.
	 * 
	 * @param attrDef
	 *            select parameters
	 * @return the builder for a new query
	 */
	QuerySpecsBuilder select(Object... attrDef);

	/**
	 * Clears all the contents for the specified type.
	 * 
	 * @param type
	 *            is the type that is to be cleared.
	 */
	void clear(CMEntryType type);

}
