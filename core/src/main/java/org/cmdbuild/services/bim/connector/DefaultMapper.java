package org.cmdbuild.services.bim.connector;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;

public class DefaultMapper implements Mapper {

	private final CMDataView dataView;
	private final MapperRules mapperRules;
	private final DifferListener listener;

	public DefaultMapper(CMDataView dataView, MapperRules support,
			final CardDiffer cardDiffer) {
		this.dataView = dataView;
		this.mapperRules = support;
		this.listener = new DifferListener() {

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
				new RuntimeException("Not implemented!");
			}
		};
	}

	@Override
	public void update(Iterable<Entity> source) {
		for (Entity sourceEntity : source) {
			final CMCard matchingCard = fetchMatchingCard(sourceEntity);
			if (matchingCard != null) {
				listener.updateTarget(sourceEntity, matchingCard);
			} else {
				listener.createTarget(sourceEntity);
			}
		}

	}

	private CMCard fetchMatchingCard(Entity sourceEntity) {
		final String className = sourceEntity.getTypeName();
		final String key = sourceEntity.getKey();
		return mapperRules.fetchCardWithKey(key, className, dataView);
	}

}
