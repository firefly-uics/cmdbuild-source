package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.WsType;

public class SharkWsFunctionTypeConverter extends SharkWsEntryTypeConverter {

	public SharkWsFunctionTypeConverter(final CachedWsSchemaApi schemaApi) {
		super(schemaApi);
	}

	@Override
	protected WsType getWsType(final String functionName, final String attributeName) {
		return WsType.UNKNOWN;
	}

}
