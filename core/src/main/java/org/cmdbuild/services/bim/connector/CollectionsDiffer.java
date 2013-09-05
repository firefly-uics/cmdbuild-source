package org.cmdbuild.services.bim.connector;

import java.util.Iterator;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class compares source and target sets and notify a listener the C-U-D
 * action to be performed.
 * */
public class CollectionsDiffer implements Differ {

	private final Iterable<Entity> source;
	private final Iterable<Entity> target;

	private static final Logger logger = LoggerFactory.getLogger(CollectionsDiffer.class);

	/**
	 * @param source
	 *            : a list of entities to be mapped onto CMDBuild cards
	 * @param target
	 *            : a list of CMDBuild cards to update with the data of source.
	 *            It can be empty.
	 * */
	public CollectionsDiffer(final Iterable<Entity> source, final Iterable<Entity> target) {
		if (source == null || target == null) {
			throw new BimError("source or target not initialised!");
		}
		this.source = source;
		this.target = target;
	}

	/**
	 * This method iterates over source and find entities to create or update;
	 * then it iterates over target and find entities to delete. FIXME: The
	 * algorithm has quadratic complexity. It should be made linear.
	 * 
	 * @param listener
	 *            : a listener which performs prescribed actions when a card to
	 *            create, update or delete is found
	 * */
	@Override
	public void findDifferences(DifferListener listener) {
		logger.info("Find entities to create or update...");
		for (Iterator<Entity> it = source.iterator(); it.hasNext();) {
			Entity sourceElement = it.next();
			String key = sourceElement.getKey();
			Entity destinationElement = searchMatchingEntity(key, target);
			if (destinationElement.isValid()) {
				listener.updateTarget(sourceElement, destinationElement);
			} else {
				listener.addTarget(sourceElement);
			}
		}
		logger.info("Done");
		logger.info("Find entities to delete...");
		for (Iterator<Entity> it = target.iterator(); it.hasNext();) {
			Entity destinationElement = it.next();
			String key = destinationElement.getKey();
			Entity sourceElement = searchMatchingEntity(key, source);
			if (!sourceElement.isValid()) {
				listener.removeTarget(destinationElement);
			}
		}
		logger.info("Done");
	}

	/**
	 * @param key
	 *            : the key (GlobalId) of some entity
	 * @param set
	 *            : the set of entities within searching the entity with the
	 *            given key
	 * @return the entity of set whose key matches the value of key parameter
	 * */
	private Entity searchMatchingEntity(String key, Iterable<Entity> set) {
		Entity correspondingEntity = Entity.NULL_ENTITY;
		for (Iterator<Entity> it = set.iterator(); it.hasNext();) {
			Entity element = it.next();
			String correspondingKey = element.getKey();
			if (correspondingKey.equals(key)) {
				correspondingEntity = element;
				break;
			}
		}
		return correspondingEntity;
	}

}