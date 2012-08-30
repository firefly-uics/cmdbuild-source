package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.ws.EntryTypeAttribute;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.EntryTypeConverter;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.WsType;
import org.cmdbuild.workflow.api.CachedWsSchemaApi.AttributeInfo;

public class SharkWsEntryTypeConverter extends SharkWsTypeConverter implements EntryTypeConverter {

	protected final CachedWsSchemaApi cachedWsSchemaApi;

	public SharkWsEntryTypeConverter(final CachedWsSchemaApi cachedWsSchemaApi) {
		this.cachedWsSchemaApi = cachedWsSchemaApi;
	}

	@Override
	public String toWsType(final EntryTypeAttribute entryTypeAttribute, final Object clientValue) {
		return toWsType(getWsType(entryTypeAttribute), clientValue);
	}

	@Override
	public String toClientType(final EntryTypeAttribute entryTypeAttribute, final String wsValue) {
		return toClientType(getWsType(entryTypeAttribute), wsValue);
	}

	private WsType getWsType(final EntryTypeAttribute entryTypeAttribute) {
		final AttributeInfo attributeInfo = cachedWsSchemaApi.findAttributeFor(entryTypeAttribute);
		return attributeInfo.getWsType();
	}

}
