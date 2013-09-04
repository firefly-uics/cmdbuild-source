package org.cmdbuild.services.bim.connector;

import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.utils.LoggerSupport;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.slf4j.Logger;

public class ConnectorCardDiffer {

	private final CMDataView dataView;

	private static final Logger logger = LoggerSupport.logger;

	public ConnectorCardDiffer(final CMDataView dataView) {
		this.dataView = dataView;
	}

	public void updateCard(final Entity source, final Entity target) throws Exception {
	}

	public void createCard(Entity source) {
		String cmdbClassName = source.getTypeName();
		CMClass theClass = dataView.findClass(cmdbClassName);
		Iterable<? extends CMAttribute> attributes = theClass.getAttributes();
		logger.info("Building card of type " + cmdbClassName);
		boolean sendDelta = false;

		CMCardDefinition card = dataView.createCardFor(theClass);

		for (CMAttribute attribute : attributes) {
			String attributeName = attribute.getName();
			boolean isReference = false;
			if (attribute.getType() instanceof ReferenceAttributeType) {
				isReference = true;
			}
			Attribute sourceAttribute = source.getAttributeByName(attributeName);
			if (sourceAttribute.isValid()) {
				if (isReference) {
					int id = findIdFromGuid(((BimAttribute) sourceAttribute).getValue(),
							attribute.getForeignKeyDestinationClassName());
					if (id != 0) {
						((BimAttribute) sourceAttribute).setValue(Integer.toString(id));
					} else {
						((BimAttribute) sourceAttribute).setValue(null);
					}
				}
				logger.info("Create attribute '{}', '{}'", attributeName, sourceAttribute.getValue());
				card.set(attributeName, sourceAttribute.getValue());
				sendDelta = true;
			}
		}
		if (sendDelta) {
			card.save();
			logger.info("Card  created");
		}
	}

	private int findIdFromGuid(String value, String foreignKeyDestinationClassName) {
		// TODO Auto-generated method stub
		return 0;
	}


}
