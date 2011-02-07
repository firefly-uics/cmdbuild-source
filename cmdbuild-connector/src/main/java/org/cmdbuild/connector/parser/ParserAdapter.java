package org.cmdbuild.connector.parser;

import org.cmdbuild.connector.data.ConnectorCard;
import org.cmdbuild.connector.data.ConnectorClass;
import org.cmdbuild.connector.data.ConnectorDomain;
import org.cmdbuild.connector.data.ConnectorRelation;

public abstract class ParserAdapter implements ParserListener {

	@Override
	public void cardFound(final ParserEvent<ConnectorCard> event) {
		// stub
	}

	@Override
	public void classFound(final ParserEvent<ConnectorClass> event) {
		// stub
	}

	@Override
	public void domainFound(final ParserEvent<ConnectorDomain> event) {
		// stub
	}

	@Override
	public void relationFound(final ParserEvent<ConnectorRelation> event) {
		// stub
	}

}
