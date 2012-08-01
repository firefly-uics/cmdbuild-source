package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.ConfigurationHelper;
import org.cmdbuild.workflow.type.LookupType;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class SharkWsWorkflowApi extends SharkWorkflowApi {

	private static SchemaApi NULL_SCHEMA_API = new SchemaApi() {

		private static final String EXCEPTION_CAUSE = "Tool not yet configured";

		@Override
		public ClassInfo findClass(final String className) {
			throw new UnsupportedOperationException(EXCEPTION_CAUSE);
		}

		@Override
		public ClassInfo findClass(final int classId) {
			throw new UnsupportedOperationException(EXCEPTION_CAUSE);
		}

		@Override
		public LookupType selectLookupById(final int id) {
			throw new UnsupportedOperationException(EXCEPTION_CAUSE);
		}

		@Override
		public LookupType selectLookupByCode(final String type, final String code) {
			throw new UnsupportedOperationException(EXCEPTION_CAUSE);
		}

		@Override
		public LookupType selectLookupByDescription(final String type, final String description) {
			throw new UnsupportedOperationException(EXCEPTION_CAUSE);
		}

	};

	private ConfigurationHelper configurationHelper;

	private static FluentApi fluentApi;

	private SchemaApi schemaApi = NULL_SCHEMA_API;

	@Override
	public void configure(final CallbackUtilities cus) {
		super.configure(cus);
		configurationHelper = new ConfigurationHelper(cus);
		setProxy(configurationHelper.newProxy());
	}

	public void setProxy(final Private proxy) {
		synchronized (proxy) {
			if (fluentApi == null) {
				final FluentApiExecutor fluentApiExecutor = new WsFluentApiExecutor(proxy);
				fluentApi = new FluentApi(fluentApiExecutor);
			}
		}
		schemaApi = new CachedWsSchemaApi(proxy);
	}

	@Override
	public FluentApi fluentApi() {
		return fluentApi;
	}

	@Override
	public SchemaApi schemaApi() {
		return new SchemaApi() {

			@Override
			public ClassInfo findClass(final String className) {
				return schemaApi.findClass(className);
			}

			@Override
			public ClassInfo findClass(final int classId) {
				return schemaApi.findClass(classId);
			}

			@Override
			public LookupType selectLookupById(final int id) {
				return schemaApi.selectLookupById(id);
			}

			@Override
			public LookupType selectLookupByCode(final String type, final String code) {
				return schemaApi.selectLookupByCode(type, code);
			}

			@Override
			public LookupType selectLookupByDescription(final String type, final String description) {
				return schemaApi.selectLookupByDescription(type, description);
			}

		};
	}

}
