package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.services.bim.DefaultBimDataModelManager.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID;
import static org.cmdbuild.bim.utils.BimConstants.X_ATTRIBUTE_NAME;
import static org.cmdbuild.bim.utils.BimConstants.Y_ATTRIBUTE_NAME;
import static org.cmdbuild.bim.utils.BimConstants.Z_ATTRIBUTE_NAME;

import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
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
import org.cmdbuild.utils.bim.BimIdentifier;
import org.slf4j.Logger;

public class CardDiffer {

	private final CMDataView dataView;
	private static final Logger logger = LoggerSupport.logger;
	private final MapperSupport support;

	public CardDiffer(final CMDataView dataView, MapperSupport support) {
		this.dataView = dataView;
		this.support = support;
	}

	public void updateCard(final Entity newEntity, CMCard oldCard) {
		CMClass defaultClass = oldCard.getType();
		CMCardDefinition newCard = dataView.update(oldCard);

		String className = defaultClass.getName();

		if (!className.equals(newEntity.getTypeName())) {
			// better safe than sorry...
			return;
		}
		Iterable<? extends CMAttribute> attributes = defaultClass
				.getAttributes();
		logger.info("Updating card of type " + className);
		boolean sendDelta = false;

		for (CMAttribute attribute : attributes) {
			String attributeName = attribute.getName();

			CMAttributeType<?> attributeType = attribute.getType();
			boolean isReference = attributeType instanceof ReferenceAttributeType;
			boolean isLookup = attributeType instanceof LookupAttributeType;
			boolean attributeDiffers = false;
			Object oldAttribute = oldCard.get(attributeName);
			if (isReference
					&& newEntity.getAttributeByName(attributeName).isValid()) {
				String newReferencedGuid = newEntity.getAttributeByName(
						attributeName).getValue();
				String referencedClassName = support
						.findReferencedClassNameFromReferenceAttribute(attribute);
				Long newReferencedId = support.findMasterIdFromGuid(
						newReferencedGuid, referencedClassName);
				if (newReferencedId != null
						&& ((CardReference) oldAttribute).getId() != newReferencedId) {
					CardReference newAttribute = new CardReference(
							newReferencedId, "");
					newCard.set(attributeName, newAttribute);
					sendDelta = true;
				}

			} else if (isLookup
					&& newEntity.getAttributeByName(attributeName).isValid()) {
				String newLookupValue = newEntity.getAttributeByName(
						attributeName).getValue();
				Long newLookupId = support.findLookupIdFromDescription(
						newLookupValue, attribute);

				// check if the value changed, otherwise skip the update
				if (newLookupId != null
						&& ((CardReference) oldAttribute).getId() != newLookupId) {
					CardReference newAttribute = new CardReference(newLookupId,
							"");
					newCard.set(attributeName, newAttribute);
					sendDelta = true;
				}
			} else if (newEntity.getAttributeByName(attributeName).isValid()) {
				Object newAttribute = attributeType.convertValue(newEntity
						.getAttributeByName(attributeName).getValue());
				attributeDiffers = !newAttribute.equals(oldAttribute);
				if (attributeDiffers) {
					newCard.set(attributeName, newAttribute);
					sendDelta = true;
				}
			}
		}

		if (sendDelta) {
			newCard.save();
			logger.info("Card updated");
		}

	}

	public void createCard(Entity source) {
		final String cmdbClassName = source.getTypeName();
		CMClass defaultClass = dataView.findClass(cmdbClassName);
		CMClass bimClass = dataView.findClass(BimIdentifier.newIdentifier()
				.withName(cmdbClassName));
		Iterable<? extends CMAttribute> attributes = defaultClass
				.getAttributes();
		logger.info("Building card of type " + cmdbClassName);
		boolean sendDelta = false;

		CMCardDefinition card = dataView.createCardFor(defaultClass);

		boolean coordinatesRequired = source.getAttributeByName(X_ATTRIBUTE_NAME)
				.isValid() && source.getAttributeByName(Y_ATTRIBUTE_NAME)
				.isValid() && source.getAttributeByName(Z_ATTRIBUTE_NAME)
				.isValid();

		for (CMAttribute attribute : attributes) {
			String attributeName = attribute.getName();
			boolean isReference = attribute.getType() instanceof ReferenceAttributeType;
			boolean isLookup = attribute.getType() instanceof LookupAttributeType;
			Attribute sourceAttribute = source
					.getAttributeByName(attributeName);
			if (sourceAttribute.isValid()) {
				if (isReference) {
					String referencedClass = support
							.findReferencedClassNameFromReferenceAttribute(attribute);
					String referencedGuid = sourceAttribute.getValue();
					Long id = support.findMasterIdFromGuid(referencedGuid,
							referencedClass);
					if (id != null) {
						((BimAttribute) sourceAttribute)
								.setValue(id.toString());
					} else {
						throw new BimError("Referenced card with globalId "
								+ referencedGuid + " not found for class "
								+ referencedClass);
					}
				} else if (isLookup) {
					String newLookupValue = sourceAttribute.getValue();
					Long newLookupId = support.findLookupIdFromDescription(
							newLookupValue, attribute);
					if (newLookupId != null) {
						((BimAttribute) sourceAttribute).setValue(newLookupId
								.toString());
					} else {
						logger.warn(
								"Lookup value with description '{}' not found for attribute '{}'",
								newLookupValue, attributeName);
					}
				}
				logger.info("Create attribute '{}', '{}'", attributeName,
						sourceAttribute.getValue());
				card.set(attributeName, sourceAttribute.getValue());
				sendDelta = true;
			}
		}
		if (sendDelta) {
			CMCard defaultCard = card.save();
			logger.info("Standard-card  created");
			Long id = defaultCard.getId();
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
