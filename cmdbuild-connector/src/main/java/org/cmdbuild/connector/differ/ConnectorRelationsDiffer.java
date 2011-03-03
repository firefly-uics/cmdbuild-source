package org.cmdbuild.connector.differ;

import java.util.SortedSet;

import org.cmdbuild.connector.data.ConnectorRelation;

public class ConnectorRelationsDiffer extends AbstractCollectionDiffer<ConnectorRelation> {

	public ConnectorRelationsDiffer(final SortedSet<ConnectorRelation> customerItem,
			final SortedSet<ConnectorRelation> cmdbuildItem) {
		super(customerItem, cmdbuildItem);
	}

	@Override
	protected AbstractItemDiffer<ConnectorRelation> getItemDiffer(final ConnectorRelation customerElement,
			final ConnectorRelation cmdbuildElement) {
		return new ConnectorRelationDiffer(customerElement, cmdbuildElement);
	}

}
