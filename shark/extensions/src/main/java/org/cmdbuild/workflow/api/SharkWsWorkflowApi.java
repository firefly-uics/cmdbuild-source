package org.cmdbuild.workflow.api;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;

import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.PasswordType;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.SoapClientBuilder;
import org.cmdbuild.services.soap.client.SoapClient;
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

	private static final String CMDBUILD_WS_URL_PROPERTY = "org.cmdbuild.ws.url";
	private static final String CMDBUILD_WS_USERNAME_PROPERTY = "org.cmdbuild.ws.username";
	private static final String CMDBUILD_WS_PASSWORD_PROPERTY = "org.cmdbuild.ws.password";

	private static final String URL_SEPARATOR = "/";
	private static final String URL_SUFFIX = "services/soap/Private";

	private static FluentApi fluentApi;

	private SchemaApi schemaApi = NULL_SCHEMA_API;

	@Override
	public void configure(final CallbackUtilities cus) {
		super.configure(cus);
		setProxy(newProxy(cus));
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

	private Private newProxy(final CallbackUtilities cus) {
		final String base_url = cus.getProperty(CMDBUILD_WS_URL_PROPERTY);
		final String url = completeUrl(base_url);
		final String username = cus.getProperty(CMDBUILD_WS_USERNAME_PROPERTY);
		final String password = cus.getProperty(CMDBUILD_WS_PASSWORD_PROPERTY);

		cus.info(null, format("creating soap client for url '%s', username '%s', password '%s'", //
				url, //
				username, //
				password));

		final SoapClient<Private> soapClient = new SoapClientBuilder<Private>() //
				.forClass(Private.class) //
				.withUrl(url) //
				.withUsername(username) //
				.withPasswordType(PasswordType.DIGEST) //
				.withPassword(password) //
				.build();
		return soapClient.getProxy();
	}

	private String completeUrl(final String baseUrl) {
		return new StringBuilder(baseUrl) //
				.append(baseUrl.endsWith(URL_SEPARATOR) ? EMPTY : URL_SEPARATOR) //
				.append(URL_SUFFIX) //
				.toString();
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

	@Override
	public WorkflowApi workflowApi() {
		return new WorkflowApi() {

		};
	}

}
