package org.cmdbuild.connector.parser;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.cmdbuild.connector.collections.ConnectorSchemaCollection;
import org.cmdbuild.connector.data.ConnectorCard;
import org.cmdbuild.connector.data.ConnectorClass;
import org.cmdbuild.connector.data.ConnectorDomain;
import org.cmdbuild.connector.data.ConnectorRelation;
import org.cmdbuild.connector.logger.Log;

public abstract class AbstractParser implements Parser {

	protected static final Logger logger = Log.PARSER;

	protected final Set<ParserListener> listeners;

	protected ConnectorSchemaCollection schema;

	public AbstractParser() {
		listeners = new HashSet<ParserListener>();
	}

	@Override
	public void addListener(final ParserListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(final ParserListener listener) {
		listeners.remove(listener);
	}

	protected void fireClassFound(final ConnectorClass connectorClass) {
		Validate.notNull(connectorClass, "null class");
		final ParserEvent<ConnectorClass> event = new ParserEvent<ConnectorClass>(this, connectorClass);
		for (final ParserListener listener : listeners) {
			listener.classFound(event);
		}
	}

	protected void fireCardFound(final ConnectorCard connectorCard) {
		Validate.notNull(connectorCard, "null card");
		final ParserEvent<ConnectorCard> event = new ParserEvent<ConnectorCard>(this, connectorCard);
		for (final ParserListener listener : listeners) {
			listener.cardFound(event);
		}

	}

	protected void fireDomainFound(final ConnectorDomain connectorDomain) {
		Validate.notNull(connectorDomain, "null domain");
		final ParserEvent<ConnectorDomain> event = new ParserEvent<ConnectorDomain>(this, connectorDomain);
		for (final ParserListener listener : listeners) {
			listener.domainFound(event);
		}

	}

	protected void fireRelationFound(final ConnectorRelation connectorRelation) {
		Validate.notNull(connectorRelation, "null relation");
		final ParserEvent<ConnectorRelation> event = new ParserEvent<ConnectorRelation>(this, connectorRelation);
		for (final ParserListener listener : listeners) {
			listener.relationFound(event);
		}

	}

	@Override
	public void setSchema(final ConnectorSchemaCollection schema) {
		this.schema = schema;
	}

}