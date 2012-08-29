package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.EntryTypeConverter;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.WsType;

public abstract class SharkWsEntryTypeConverter extends SharkWsTypeConverter implements EntryTypeConverter {

	protected final CachedWsSchemaApi schemaApi;

	protected SharkWsEntryTypeConverter(final CachedWsSchemaApi schemaApi) {
		this.schemaApi = schemaApi;
	}

	@Override
	public String toWsType(final String entryTypeName, final String attributeName, final Object clientValue) {
		return toWsType(getWsType(entryTypeName, attributeName), clientValue);
	}

	@Override
	public String toClientType(final String entryTypeName, final String attributeName, final String wsValue) {
		return toClientType(getWsType(entryTypeName, attributeName), attributeName, wsValue);
	}

	protected abstract WsType getWsType(String entryTypeName, String attributeName);

}
