package org.cmdbuild.services.bim.connector;

import java.util.Iterator;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;

public class Mapper {

	private final CMDataView dataView;
	private final MapperSupport support;

	public Mapper(CMDataView dataView) {
		this.dataView = dataView;
		support = new MapperSupport(dataView);
	}

	public void update(Iterable<Entity> source) {
		DifferListener listener = new DifferListener() {

			/** perform CUD actions on CMDBuild */
			private final CardDiffer cardDiffer = new CardDiffer(dataView);

			@Override
			public void createTarget(Entity source) {
				cardDiffer.createCard(source);
			}

			@Override
			public void updateTarget(Entity source, CMCard target) {
				cardDiffer.updateCard(source, target);
			}

			@Override
			public void deleteTarget(CMCard target) {
				// cardDiffer.deleteCard(target);
			}

		};

		for (Iterator<Entity> it = source.iterator(); it.hasNext();) {
			Entity sourceElement = it.next();
			String className = sourceElement.getTypeName();
			String key = sourceElement.getKey();
			CMCard oldCard = support.fetchCardFromGlobalIdAndClassName(key, className);
			if (oldCard != null) {
				listener.updateTarget(sourceElement, oldCard);
			} else {
				listener.createTarget(sourceElement);
			}
		}

	}

}
