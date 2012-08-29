package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.WsType;

public class SharkWsCardTypeConverter extends SharkWsEntryTypeConverter {

	public SharkWsCardTypeConverter(final CachedWsSchemaApi schemaApi) {
		super(schemaApi);
	}

	@Override
	protected WsType getWsType(final String className, final String attributeName) {
		return WsType.UNKNOWN;
	}

}
