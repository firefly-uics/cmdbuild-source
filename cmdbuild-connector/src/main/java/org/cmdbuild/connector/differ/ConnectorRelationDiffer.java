package org.cmdbuild.connector.differ;

import org.cmdbuild.connector.data.ConnectorRelation;

public class ConnectorRelationDiffer extends AbstractItemDiffer<ConnectorRelation> {

	public ConnectorRelationDiffer(final ConnectorRelation customerRelation, final ConnectorRelation cmdbuildRelation) {
		super(customerRelation, cmdbuildRelation);
	}

	@Override
	protected void diff(final ConnectorRelation customerItem, final ConnectorRelation cmdbuildItem) {
		// nothing to do
	}

}
