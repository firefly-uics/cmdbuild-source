package org.cmdbuild.workflow.api;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor;
import org.cmdbuild.common.Constants;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.PasswordType;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.SoapClientBuilder;
import org.cmdbuild.services.soap.client.SoapClient;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
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

	private static final String SOAP_OPERATOR_EQUALS = "EQUALS";
	private static final List<Attribute> SOAP_ALL_ATTRIBUTES = null;
	private static final List<Order> SOAP_NO_ORDERING = null;
	private static final String SOAP_NO_FULLTEXT = null;
	private static final CqlQuery SOAP_NO_CQL = null;

	private static FluentApi fluentApi;

	private Private proxy;
	private SchemaApi schemaApi = NULL_SCHEMA_API;

	@Override
	public void configure(final CallbackUtilities cus) {
		super.configure(cus);
		setProxy(newProxy(cus));
	}

	public void setProxy(final Private proxy) {
		this.proxy = proxy;
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
			@Override
			public ReferenceType selectReference(final String className, final String attributeName,
					final String attributeValue) {
				final int limit = 1, offset = 0;
				final List<Card> cardList = proxy.getCardList(className, SOAP_ALL_ATTRIBUTES,
						filterByAttribute(attributeName, attributeValue), //
						SOAP_NO_ORDERING, limit, offset, SOAP_NO_FULLTEXT, SOAP_NO_CQL).getCards();
				if (cardList.isEmpty()) {
					return new ReferenceType();
				} else {
					return referenceType(cardList.get(0));
				}
			}

			@Override
			public String selectAttribute(final String className, final int cardId, final String attributeName) {
				final Card card = proxy.getCard(className, cardId, SOAP_ALL_ATTRIBUTES);
				for (final Attribute a : card.getAttributeList()) {
					if (StringUtils.equals(attributeName, a.getName())) {
						return a.getValue();
					}
				}
				return null;
			}

		};
	}

	private ReferenceType referenceType(final Card card) {
		final ReferenceType rt = new ReferenceType();
		rt.setId(card.getId());
		for (final Attribute attribute : card.getAttributeList()) {
			final String attributeName = attribute.getName();
			if (Constants.CLASS_ID_ATTRIBUTE.equals(attributeName)) {
				final int classId = Integer.parseInt(attribute.getValue());
				rt.setIdClass(classId);
			} else if (Constants.DESCRIPTION_ATTRIBUTE.equals(attributeName)) {
				rt.setDescription(attribute.getValue());
			}
		}
		return rt;
	}

	private Query filterByAttribute(final String attributeName, final String attributeValue) {
		final Filter filter = new Filter();
		filter.setName(attributeName);
		filter.setOperator(SOAP_OPERATOR_EQUALS);
		filter.getValue().add(attributeValue);
		final Query query = new Query();
		query.setFilter(filter);
		return query;
	}

}
