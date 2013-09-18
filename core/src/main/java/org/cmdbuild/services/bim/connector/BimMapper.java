package org.cmdbuild.services.bim.connector;

import javax.sql.DataSource;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.springframework.jdbc.core.JdbcTemplate;

public class BimMapper implements Mapper {

	private final CMDataView dataView;
	private final LookupLogic lookupLogic;
	private final JdbcTemplate jdbcTemplate;
	private final MapperSupport support;

	public BimMapper(CMDataView dataView, LookupLogic lookupLogic,
			DataSource dataSource) {
		this.dataView = dataView;
		this.lookupLogic = lookupLogic;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		support = new MapperSupport(jdbcTemplate);
	}

	@Override
	public void update(Iterable<Entity> source) {
		DifferListener listener = new DifferListener() {

			/** perform CUD actions on CMDBuild */
			private final CardDiffer cardDiffer = new CardDiffer(dataView,
					support, lookupLogic, jdbcTemplate);

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
		return MapperSupport.fetchCardWithKey(key, className, dataView);
	}

}
