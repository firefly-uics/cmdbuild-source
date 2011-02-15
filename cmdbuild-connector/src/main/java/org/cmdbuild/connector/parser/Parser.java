package org.cmdbuild.connector.parser;

import org.cmdbuild.connector.collections.ConnectorSchemaCollection;

public interface Parser {

	public void addListener(final ParserListener listener);

	public void removeListener(final ParserListener listener);

	public void setSchema(final ConnectorSchemaCollection schema);

	public void parseSchema() throws ParserException;

	public void parse() throws ParserException;

}