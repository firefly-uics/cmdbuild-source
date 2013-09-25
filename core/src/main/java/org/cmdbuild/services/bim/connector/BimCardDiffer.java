package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.bim.utils.BimConstants.BIM_SCHEMA_NAME;
import static org.cmdbuild.bim.utils.BimConstants.COORDINATES;
import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GEOMETRY_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID;
import static org.cmdbuild.bim.utils.BimConstants.STORE_COORDINATES_QUERY_TEMPLATE;
import static org.cmdbuild.bim.utils.BimConstants.SPACEGEOMETRY;
import static org.cmdbuild.bim.utils.BimConstants.SPACEHEIGHT;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.services.bim.DefaultBimDataModelManager.HEIGHT;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.springframework.jdbc.core.JdbcTemplate;

public class BimCardDiffer implements CardDiffer {

	private CardDiffer defaultCardDiffer;
	private JdbcTemplate jdbcTemplate; // Perhaps we will not need it.
	private final CMDataView dataView;

	private BimCardDiffer(final CMDataView dataView, LookupLogic lookupLogic, JdbcTemplate jdbcTemplate) {
		this.defaultCardDiffer = new DefaultCardDiffer(dataView, lookupLogic, BimMapperRules.INSTANCE);
		this.jdbcTemplate = jdbcTemplate;
		this.dataView = dataView;
	}

	public static BimCardDiffer buildBimCardDiffer(CMDataView dataView, LookupLogic lookupLogic,
			JdbcTemplate jdbcTemplate) {
		return new BimCardDiffer(dataView, lookupLogic, jdbcTemplate);
	}

	@Override
	public CMCard updateCard(Entity sourceEntity, CMCard oldCard) {
		CMCard updatedCard = defaultCardDiffer.updateCard(sourceEntity, oldCard);
	

			CMClass bimClass = dataView.findClass(BimIdentifier.newIdentifier().withName(sourceEntity.getTypeName()));
			CMQueryResult queryResult = dataView.select(anyAttribute(bimClass))//
					.from(bimClass)//
					.where(condition(attribute(bimClass, GLOBALID), eq(sourceEntity.getKey()))).run();
			CMCard bimCard = queryResult.getOnlyRow().getCard(bimClass);
			boolean updateCoordinates = sourceEntity.getAttributeByName(COORDINATES).isValid();
			boolean updateSpaceGeometry = sourceEntity.getAttributeByName(SPACEGEOMETRY).isValid()
					&& sourceEntity.getAttributeByName(SPACEHEIGHT).isValid();
			if (updateCoordinates) {
				final String coordinates = sourceEntity.getAttributeByName(COORDINATES).getValue();
				final String updateCoordinatesQuery = String.format(STORE_COORDINATES_QUERY_TEMPLATE, //
						BIM_SCHEMA_NAME, //
						sourceEntity.getTypeName(), //
						GEOMETRY_ATTRIBUTE, //
						coordinates,//
						ID_ATTRIBUTE, //
						bimCard.getId() //
						);
				System.out.println(updateCoordinatesQuery);
				jdbcTemplate.update(updateCoordinatesQuery);
			} else if (updateSpaceGeometry) {
				final String polygon = sourceEntity.getAttributeByName(SPACEGEOMETRY).getValue();
				final String height = sourceEntity.getAttributeByName(SPACEHEIGHT).getValue();
				final String updateGeometryQuery = String.format(STORE_COORDINATES_QUERY_TEMPLATE, //
						BIM_SCHEMA_NAME, //
						sourceEntity.getTypeName(), //
						GEOMETRY_ATTRIBUTE, //
						polygon,//
						ID_ATTRIBUTE, //
						bimCard.getId());
				System.out.println(updateGeometryQuery);
				jdbcTemplate.update(updateGeometryQuery);

				CMCardDefinition cardDefinition = dataView.update(bimCard);
				cardDefinition.set(HEIGHT, height);
				cardDefinition.save();
			}
		
		return updatedCard;
	}

	@Override
	public CMCard createCard(Entity sourceEntity) {
		CMCard newCard = defaultCardDiffer.createCard(sourceEntity);
		if (newCard != null) {
			CMCard bimCard = createBimCard(newCard, sourceEntity);
			boolean storeCoordinates = sourceEntity.getAttributeByName(COORDINATES).isValid();
			boolean storeSpaceGeometry = sourceEntity.getAttributeByName(SPACEGEOMETRY).isValid()
					&& sourceEntity.getAttributeByName(SPACEHEIGHT).isValid();
			if (storeCoordinates) {
				final String coordinates = sourceEntity.getAttributeByName(COORDINATES).getValue();
				final String updateCoordinatesQuery = String.format(STORE_COORDINATES_QUERY_TEMPLATE, //
						BIM_SCHEMA_NAME, //
						sourceEntity.getTypeName(), //
						GEOMETRY_ATTRIBUTE, //
						coordinates,//
						ID_ATTRIBUTE, //
						bimCard.getId() //
						);
				System.out.println(updateCoordinatesQuery);
				jdbcTemplate.update(updateCoordinatesQuery);
			} else if (storeSpaceGeometry) {
				final String polygon = sourceEntity.getAttributeByName(SPACEGEOMETRY).getValue();
				final String height = sourceEntity.getAttributeByName(SPACEHEIGHT).getValue();
				final String updateGeometryQuery = String.format(STORE_COORDINATES_QUERY_TEMPLATE, //
						BIM_SCHEMA_NAME, //
						sourceEntity.getTypeName(), //
						GEOMETRY_ATTRIBUTE, //
						polygon,//
						ID_ATTRIBUTE, //
						bimCard.getId());
				System.out.println(updateGeometryQuery);
				jdbcTemplate.update(updateGeometryQuery);

				CMCardDefinition cardDefinition = dataView.update(bimCard);
				cardDefinition.set(HEIGHT, height);
				cardDefinition.save();
			}
		}
		return newCard;
	}

	private CMCard createBimCard(CMCard newCard, Entity sourceEntity) {
		String cmdbClassName = sourceEntity.getTypeName();
		Long id = newCard.getId();
		CMClass bimClass = dataView.findClass(BimIdentifier.newIdentifier().withName(cmdbClassName));
		CMCardDefinition bimCard = dataView.createCardFor(bimClass);
		bimCard.set(GLOBALID, sourceEntity.getKey());
		bimCard.set(FK_COLUMN_NAME, id.toString());
		return bimCard.save();
	}

}
