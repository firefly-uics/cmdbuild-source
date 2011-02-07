package org.cmdbuild.connector.differ;

import org.cmdbuild.connector.data.ConnectorCard;
import org.cmdbuild.connector.data.ConnectorCardAttribute;
import org.cmdbuild.connector.data.ConnectorClass;
import org.cmdbuild.connector.data.ConnectorClassAttribute;
import org.cmdbuild.connector.data.Key;
import org.cmdbuild.connector.data.MutableConnectorCard;

public class ConnectorCardDiffer extends AbstractItemDiffer<ConnectorCard> {

	public ConnectorCardDiffer(final ConnectorCard customerCard, final ConnectorCard cmdbuildCard) {
		super(customerCard, cmdbuildCard);
	}

	@Override
	protected void diff(final ConnectorCard customerItem, final ConnectorCard cmdbuildItem) {
		final ConnectorClass connectorClass = customerItem.getConnectorClass();
		final MutableConnectorCard delta = new MutableConnectorCard(connectorClass);
		boolean sendDelta = false;
		for (final ConnectorClassAttribute classAttribute : connectorClass.getAttributes()) {
			final String name = classAttribute.getName();
			logger.debug("checking attribute '" + name + "'");
			final ConnectorCardAttribute customerAttribute = customerItem.getAttribute(name);
			final ConnectorCardAttribute cmdbuildAttribute = cmdbuildItem.getAttribute(name);
			if (classAttribute.isKey()) {
				logger.debug("copying key attribute '" + name + "'");
				copyAttribute(customerAttribute, delta);
			} else if (attributeDiffers(customerAttribute, cmdbuildAttribute)) {
				logger.debug("copying different attribute '" + name + "'");
				copyAttribute(customerAttribute, delta);
				sendDelta = true;
			}
		}

		if (sendDelta) {
			fireModifyItem(delta);
		}
	}

	private static void copyAttribute(final ConnectorCardAttribute attribute, final MutableConnectorCard target) {
		final String name = attribute.getName();
		if (attribute.isReference()) {
			target.setAttribute(name, attribute.getReference());
		} else {
			target.setAttribute(name, attribute.getValue());
		}
	}

	private static boolean attributeDiffers(final ConnectorCardAttribute customerAttribute,
			final ConnectorCardAttribute cmdbuildAttribute) {
		final boolean differs;
		if (customerAttribute.isReference()) {
			differs = new AttributeDiffer<Key>(customerAttribute.getReference(), cmdbuildAttribute.getReference())
					.diff();
		} else {
			differs = new AttributeDiffer<String>(customerAttribute.getValue(), cmdbuildAttribute.getValue()).diff();
		}
		return differs;
	}

	private static class AttributeDiffer<E> {
		private final E element1;
		private final E element2;

		public AttributeDiffer(final E element1, final E element2) {
			this.element1 = element1;
			this.element2 = element2;
		}

		public boolean diff() {
			final boolean differs;
			differs = !element1.equals(element2);
			if (logger.isDebugEnabled() && differs) {
				logger.debug(String.format("'%s' vs. '%s'", element1, element2));
			}
			return differs;
		}
	}

}
