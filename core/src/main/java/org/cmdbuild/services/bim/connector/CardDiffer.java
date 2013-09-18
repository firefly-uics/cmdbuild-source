package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.bim.utils.BimConstants.GLOBALID;
import static org.cmdbuild.bim.utils.BimConstants.X_ATTRIBUTE_NAME;
import static org.cmdbuild.bim.utils.BimConstants.Y_ATTRIBUTE_NAME;
import static org.cmdbuild.bim.utils.BimConstants.Z_ATTRIBUTE_NAME;
import static org.cmdbuild.services.bim.DefaultBimDataModelManager.FK_COLUMN_NAME;

import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.utils.LoggerSupport;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.CardReference;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

public class CardDiffer {

	private final CMDataView dataView;
	private final LookupLogic lookupLogic;
	private final JdbcTemplate jdbcTemplate;
	private final MapperSupport support;
	private static final Logger logger = LoggerSupport.logger;

	public CardDiffer(final CMDataView dataView, MapperSupport support,
			LookupLogic lookupLogic, JdbcTemplate jdbcTemplate) {
		this.dataView = dataView;
		this.support = support;
		this.jdbcTemplate = jdbcTemplate;
		this.lookupLogic = lookupLogic;
	}

	public void updateCard(final Entity sourceEntity, final CMCard oldCard) {
		final CMClass theClass = oldCard.getType();
		CMCardDefinition cardDefinition = dataView.update(oldCard);

		final String className = theClass.getName();
		if (!className.equals(sourceEntity.getTypeName())) {
			// better safe than sorry...
			return;
		}

		Iterable<? extends CMAttribute> attributes = theClass.getAttributes();
		logger.info("Updating card " + oldCard.getId() + " of type "
				+ className);
		boolean sendDelta = false;
		// TODO update of geometric-bim attributes not managed!!
		for (final CMAttribute attribute : attributes) {
			final String attributeName = attribute.getName();

			CMAttributeType<?> attributeType = attribute.getType();
			final boolean isReference = attributeType instanceof ReferenceAttributeType;
			final boolean isLookup = attributeType instanceof LookupAttributeType;
			final Object oldAttribute = oldCard.get(attributeName);

			if (sourceEntity.getAttributeByName(attributeName).isValid()) {
				if (isReference || isLookup) {
					final CardReference oldReference = (CardReference) oldAttribute;
					Long newReferencedId = null;
					if (isReference) {
						final String referencedClass = MapperSupport
								.findReferencedClassNameFromReferenceAttribute(
										attribute, dataView);
						final String newReferencedKey = sourceEntity
								.getAttributeByName(attributeName).getValue();
						newReferencedId = MapperSupport.findIdFromKey(
								newReferencedKey, referencedClass, dataView);
					} else if (isLookup) {
						final String lookupType = ((LookupAttributeType) attribute
								.getType()).getLookupTypeName();
						final String newLookupValue = sourceEntity
								.getAttributeByName(attributeName).getValue();
						newReferencedId = MapperSupport
								.findLookupIdFromDescription(newLookupValue,
										lookupType, lookupLogic);
					}
					if (newReferencedId != null
							&& !newReferencedId.equals(oldReference.getId())) {
						final CardReference newReference = new CardReference(
								newReferencedId, "");
						cardDefinition.set(attributeName, newReference);
						sendDelta = true;
					}
				} else {
					final Object newAttribute = attributeType
							.convertValue(sourceEntity.getAttributeByName(
									attributeName).getValue());
					if (!newAttribute.equals(oldAttribute)) {
						cardDefinition.set(attributeName, newAttribute);
						sendDelta = true;
					}
				}
			}
		}
		if (sendDelta) {
			cardDefinition.save();
			logger.info("Card updated");
		}

	}

	public void createCard(Entity source) {
		final String className = source.getTypeName();
		final CMClass theClass = dataView.findClass(className);
		CMCardDefinition cardDefinition = dataView.createCardFor(theClass);

		final CMClass bimClass = dataView.findClass(BimIdentifier
				.newIdentifier().withName(className));

		Iterable<? extends CMAttribute> attributes = theClass.getAttributes();
		logger.info("Building card of type " + className);
		boolean sendDelta = false;
		// FIXME
		boolean coordinatesRequired = source.getAttributeByName(
				X_ATTRIBUTE_NAME).isValid()
				&& source.getAttributeByName(Y_ATTRIBUTE_NAME).isValid()
				&& source.getAttributeByName(Z_ATTRIBUTE_NAME).isValid();

		for (CMAttribute attribute : attributes) {
			final String attributeName = attribute.getName();
			final boolean isReference = attribute.getType() instanceof ReferenceAttributeType;
			final boolean isLookup = attribute.getType() instanceof LookupAttributeType;
			Attribute sourceAttribute = source
					.getAttributeByName(attributeName);
			if (sourceAttribute.isValid()) {
				if (isReference || isLookup) {
					Long newReferencedId = null;
					if (isReference) {
						String referencedClass = MapperSupport
								.findReferencedClassNameFromReferenceAttribute(
										attribute, dataView);
						String referencedGuid = sourceAttribute.getValue();
						newReferencedId = MapperSupport.findIdFromKey(
								referencedGuid, referencedClass, dataView);
					} else if (isLookup) {
						String newLookupValue = sourceAttribute.getValue();
						final String lookupType = ((LookupAttributeType) attribute
								.getType()).getLookupTypeName();
						newReferencedId = MapperSupport
								.findLookupIdFromDescription(newLookupValue,
										lookupType, lookupLogic);
					}
					if (newReferencedId != null) {
						sourceAttribute.setValue(newReferencedId.toString());
						cardDefinition.set(attributeName,
								sourceAttribute.getValue());
						sendDelta = true;
					}
				} else {
					cardDefinition.set(attributeName,
							sourceAttribute.getValue());
					sendDelta = true;
				}
			}
		}
		if (sendDelta) {
			CMCard card = cardDefinition.save();
			logger.info("Card  created");
			Long id = card.getId();
			CMCardDefinition bimCard = dataView.createCardFor(bimClass);
			bimCard.set(GLOBALID, source.getKey());
			bimCard.set(FK_COLUMN_NAME, id.toString());
			CMCard storedCard = bimCard.save();
			if (coordinatesRequired) {
				support.storeCoordinates(storedCard, source);
			}
			logger.info("Bim-card  created");
		}
	}

}
