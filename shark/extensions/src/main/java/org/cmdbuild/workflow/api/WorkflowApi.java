package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.type.LookupType;

public class WorkflowApi extends FluentApi implements SchemaApi {

	private CachedWsSchemaApi cachedSchemaApi;

	/**
	 * It's really ugly but fortunately all is hidden behind the
	 * {@link SharkWorkflowApiFactory}.
	 */
	public WorkflowApi(final FluentApiExecutor executor, final Private proxy) {
		super(executor);
	}

	@Override
	public ClassInfo findClass(final String className) {
		return cachedSchemaApi.findClass(className);
	}

	@Override
	public ClassInfo findClass(final int classId) {
		return cachedSchemaApi.findClass(classId);
	}

	@Override
	public LookupType selectLookupById(final int id) {
		return cachedSchemaApi.selectLookupById(id);
	}

	@Override
	public LookupType selectLookupByCode(final String type, final String code) {
		return cachedSchemaApi.selectLookupByCode(type, code);
	}

	@Override
	public LookupType selectLookupByDescription(final String type, final String description) {
		return cachedSchemaApi.selectLookupByDescription(type, description);
	}

}
