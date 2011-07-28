package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.QuerySpecsBuilder;

/**
 * This interface provides an abstract view over the data model.
 */
public interface CMDataView {

//	CMClassDefinition newClass(String name);
	CMClass findClass(Object idOrName);
	CMClass findClassById(Object id);
	CMClass findClassByName(String name);

	/**
	 * Returns the active classes.
	 * 
	 * @return active classes
	 */
	Iterable<? extends CMClass> findClasses();

	/**
	 * Returns all (active and inactive) classes. It should be used by the
	 * Database Designer only.
	 * 
	 * @return all classes (active and inactive)
	 */
	Iterable<? extends CMClass> findAllClasses();

	CMDomain findDomain(Object idOrName);
	CMDomain findDomainById(Object id);
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
	 * @param type the class i'm requesting the domains for
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

	/**
	 * Returns an empty card to be modified and saved. 
	 * 
	 * Note: it does not create a card in the data store until
	 * {@link CMCardDefinition#save()} is called on the resulting object.
	 * 
	 * @param type class for the card
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
	 * @param card immutable card to be modified
	 * 
	 * @return a modifiable card from the immutable card
	 */
	CMCardDefinition modifyCard(CMCard card);


	QuerySpecsBuilder select(Object... attrDef);
}
