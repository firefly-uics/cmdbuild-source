package org.cmdbuild.connector.differ;

import java.util.SortedSet;

import org.cmdbuild.connector.data.ConnectorCard;

public class ConnectorCardsDiffer extends AbstractCollectionDiffer<ConnectorCard> {

	public ConnectorCardsDiffer(final SortedSet<ConnectorCard> customerItem, final SortedSet<ConnectorCard> cmdbuildItem) {
		super(customerItem, cmdbuildItem);
	}

	@Override
	protected AbstractItemDiffer<ConnectorCard> getItemDiffer(final ConnectorCard customerElement,
			final ConnectorCard cmdbuildElement) {
		return new ConnectorCardDiffer(customerElement, cmdbuildElement);
	}

	@Override
	protected void preCompareElements(final ConnectorCard customerElement, final ConnectorCard cmdbuildElement) {
		logger.debug(String.format("comparing elements: '%s' vs. '%s'", customerElement.getKey(), cmdbuildElement
				.getKey()));
	}

}
