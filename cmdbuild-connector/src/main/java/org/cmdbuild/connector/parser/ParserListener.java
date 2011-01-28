package org.cmdbuild.connector.parser;

import java.util.EventListener;

import org.cmdbuild.connector.data.ConnectorCard;
import org.cmdbuild.connector.data.ConnectorClass;
import org.cmdbuild.connector.data.ConnectorDomain;
import org.cmdbuild.connector.data.ConnectorRelation;

public interface ParserListener extends EventListener {

	public void classFound(final ParserEvent<ConnectorClass> event);

	public void cardFound(final ParserEvent<ConnectorCard> event);

	public void domainFound(final ParserEvent<ConnectorDomain> event);

	public void relationFound(final ParserEvent<ConnectorRelation> event);

}
