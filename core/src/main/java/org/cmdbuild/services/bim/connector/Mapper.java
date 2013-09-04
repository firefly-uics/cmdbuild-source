package org.cmdbuild.services.bim.connector;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.view.CMDataView;

public class Mapper {

	private final CMDataView dataView;

	public Mapper(CMDataView dataView) {
		this.dataView = dataView;
	}

	public void update(Iterable<Entity> source, Iterable<Entity> target) {
		DifferListener listener = new DifferListener() {

			/** perform C-U-D actions on CMDBuild */
			private final ConnectorCardDiffer cardDiffer = new ConnectorCardDiffer(dataView);

			@Override
			public void addTarget(Entity source) {

				cardDiffer.createCard(source);
			}

			@Override
			public void updateTarget(Entity source, Entity target) {

				//cardDiffer.updateCard(source, target);
			}

			@Override
			public void removeTarget(Entity target) {
				//cardDiffer.deleteCard(target);
			}

		};

		CollectionsDiffer collectionsDiffer = new CollectionsDiffer(source, target);
		collectionsDiffer.findDifferences(listener);

	}

}
