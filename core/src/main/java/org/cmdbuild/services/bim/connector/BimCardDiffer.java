package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.bim.utils.BimConstants.BIM_SCHEMA_NAME;
import static org.cmdbuild.bim.utils.BimConstants.COORDINATES;
import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.HEIGHT;
import static org.cmdbuild.bim.utils.BimConstants.PERIMETER;
import static org.cmdbuild.bim.utils.BimConstants.POSITION;
import static org.cmdbuild.bim.utils.BimConstants.SPACEGEOMETRY;
import static org.cmdbuild.bim.utils.BimConstants.SPACEHEIGHT;
import static org.cmdbuild.bim.utils.BimConstants.STORE_GEOMETRY_QUERY_TEMPLATE;
import static org.cmdbuild.bim.utils.BimConstants.UPDATE_COORDINATES_QUERY_TEMPLATE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.springframework.jdbc.core.JdbcTemplate;

public class BimCardDiffer implements CardDiffer {
	
	//TODO remove use of jdbcTemplate

	private final CardDiffer defaultCardDiffer;
	private final JdbcTemplate jdbcTemplate; // Perhaps we will not need it.
	private final CMDataView dataView;

	private BimCardDiffer(final CMDataView dataView, final LookupLogic lookupLogic, final JdbcTemplate jdbcTemplate, final BimDataView bimDataView) {
		this.defaultCardDiffer = new OptimizedDefaultCardDiffer(dataView, lookupLogic, bimDataView);
		this.jdbcTemplate = jdbcTemplate;
		this.dataView = dataView;
	}

	public static BimCardDiffer buildBimCardDiffer(final CMDataView dataView, final LookupLogic lookupLogic,
			final JdbcTemplate jdbcTemplate, BimDataView bimDataView) {
		return new BimCardDiffer(dataView, lookupLogic, jdbcTemplate, bimDataView);
	}

	@Override
	public CMCard updateCard(final Entity sourceEntity, final CMCard oldCard) {
		final CMCard updatedCard = defaultCardDiffer.updateCard(sourceEntity, oldCard);

		final CMClass bimClass = dataView.findClass(BimIdentifier.newIdentifier().withName(sourceEntity.getTypeName()));
		final CMQueryResult queryResult = dataView.select(anyAttribute(bimClass))//
				.from(bimClass)//
				.where(condition(attribute(bimClass, GLOBALID_ATTRIBUTE), eq(sourceEntity.getKey()))).run();
		final CMCard bimCard = queryResult.getOnlyRow().getCard(bimClass);
		final boolean updateCoordinates = sourceEntity.getAttributeByName(COORDINATES).isValid();
		final boolean updateSpaceGeometry = sourceEntity.getAttributeByName(SPACEGEOMETRY).isValid()
				&& sourceEntity.getAttributeByName(SPACEHEIGHT).isValid();
		if (updateCoordinates) {
			final String coordinates = sourceEntity.getAttributeByName(COORDINATES).getValue();
			final String updateCoordinatesQuery = String.format(UPDATE_COORDINATES_QUERY_TEMPLATE, //
					BIM_SCHEMA_NAME, //
					sourceEntity.getTypeName(), //
					POSITION, //
					coordinates,//
					ID_ATTRIBUTE, //
					bimCard.getId() //
					);
			System.out.println(updateCoordinatesQuery);
			jdbcTemplate.update(updateCoordinatesQuery);
		} else if (updateSpaceGeometry) {
			final String polygon = sourceEntity.getAttributeByName(SPACEGEOMETRY).getValue();
			final String height = sourceEntity.getAttributeByName(SPACEHEIGHT).getValue();
			final String updateGeometryQuery = String.format(STORE_GEOMETRY_QUERY_TEMPLATE, //
					BIM_SCHEMA_NAME, //
					sourceEntity.getTypeName(), //
					PERIMETER, //
					polygon,//
					HEIGHT, //
					height, //
					ID_ATTRIBUTE, //
					bimCard.getId());
			System.out.println(updateGeometryQuery);
			jdbcTemplate.update(updateGeometryQuery);
		}

		return updatedCard;
	}

	@Override
	public CMCard createCard(final Entity sourceEntity) {
		final CMCard newCard = defaultCardDiffer.createCard(sourceEntity);
		if (newCard != null) {
			final CMCard bimCard = createBimCard(newCard, sourceEntity);
			final boolean storeCoordinates = sourceEntity.getAttributeByName(COORDINATES).isValid();
			final boolean storeSpaceGeometry = sourceEntity.getAttributeByName(SPACEGEOMETRY).isValid()
					&& sourceEntity.getAttributeByName(SPACEHEIGHT).isValid();
			if (storeCoordinates) {
				final String coordinates = sourceEntity.getAttributeByName(COORDINATES).getValue();
				final String updateCoordinatesQuery = String.format(UPDATE_COORDINATES_QUERY_TEMPLATE, //
						BIM_SCHEMA_NAME, //
						sourceEntity.getTypeName(), //
						POSITION, //
						coordinates,//
						ID_ATTRIBUTE, //
						bimCard.getId() //
						);
				System.out.println(updateCoordinatesQuery);
				jdbcTemplate.update(updateCoordinatesQuery);
			} else if (storeSpaceGeometry) {
				final String polygon = sourceEntity.getAttributeByName(SPACEGEOMETRY).getValue();
				final String height = sourceEntity.getAttributeByName(SPACEHEIGHT).getValue();
				final String updateGeometryQuery = String.format(UPDATE_COORDINATES_QUERY_TEMPLATE, //
						BIM_SCHEMA_NAME, //
						sourceEntity.getTypeName(), //
						PERIMETER, //
						polygon,//
						ID_ATTRIBUTE, //
						bimCard.getId());
				System.out.println(updateGeometryQuery);
				jdbcTemplate.update(updateGeometryQuery);

				final CMCardDefinition cardDefinition = dataView.update(bimCard);
				cardDefinition.set(HEIGHT, height);
				cardDefinition.save();
			}
		}
		return newCard;
	}

	private CMCard createBimCard(final CMCard newCard, final Entity sourceEntity) {
		final String cmdbClassName = sourceEntity.getTypeName();
		final Long id = newCard.getId();
		final CMClass bimClass = dataView.findClass(BimIdentifier.newIdentifier().withName(cmdbClassName));
		final CMCardDefinition bimCard = dataView.createCardFor(bimClass);
		bimCard.set(GLOBALID_ATTRIBUTE, sourceEntity.getKey());
		bimCard.set(FK_COLUMN_NAME, id.toString());
		return bimCard.save();
	}

}
