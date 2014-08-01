package org.cmdbuild.data.store.dao;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.Storable;

public interface StorableConverter<T extends Storable> {

	/**
	 * @return the name of the class in the store.
	 */
	String getClassName();

	/**
	 * @return the name of the identifier attribute.
	 */
	String getIdentifierAttributeName();

	/**
	 * Converts a card into a {@link Storable}.
	 * 
	 * @param card
	 *            the cards that needs to be converted.
	 * 
	 * @return the instance of {@link Storable} representing the card.
	 */
	Storable storableOf(CMCard card);

	/**
	 * Converts a card into a {@link T}.
	 * 
	 * @param card
	 *            the cards that needs to be converted.
	 * 
	 * @return the instance of {@link T} representing the card.
	 */
	T convert(CMCard card);

	/**
	 * Converts a generic type into a map of <String, Object>, corresponding to
	 * attribute <name, value>
	 * 
	 * @param storable
	 * @return
	 */
	Map<String, Object> getValues(T storable);

	String getUser(T storable);

}