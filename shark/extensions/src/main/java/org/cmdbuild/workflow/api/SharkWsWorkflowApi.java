package org.cmdbuild.workflow.api;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.common.Constants;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.services.soap.Relation;
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
		public LookupType selectLookupById(int id) {
			throw new UnsupportedOperationException(EXCEPTION_CAUSE);
		}

		@Override
		public LookupType selectLookupByCode(String type, String code) {
			throw new UnsupportedOperationException(EXCEPTION_CAUSE);
		}

		@Override
		public LookupType selectLookupByDescription(String type, String description) {
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

	private Private proxy;
	private SchemaApi schemaApi = NULL_SCHEMA_API;

	@Override
	public void configure(final CallbackUtilities cus) {
		super.configure(cus);
		setProxy(newProxy(cus));
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

	public void setProxy(final Private proxy) {
		this.proxy = proxy;
		schemaApi = new CachedWsSchemaApi(proxy);
	}

	@Override
	public int createCard(final String className, final Map<String, Object> attributes) {
		final Card card = new Card();
		card.setClassName(className);
		for (final String name : attributes.keySet()) {
			final Object value = attributes.get(name);
			final String stringValue = convertToWsString(value);
			card.getAttributeList().add(attribute(name, stringValue));
		}
		final int id = proxy.createCard(card);
		return id;
	}

	private String convertToWsString(final Object value) {
		if (value == null) {
			return StringUtils.EMPTY;
		} else {
			return value.toString();
		}
	}

	private Attribute attribute(final String name, final String value) {
		final Attribute attribute = new Attribute();
		attribute.setName(name);
		attribute.setValue(value);
		return attribute;
	}

	@Override
	public void createRelation(final String domainName, final String className1, final int id1,
			final String className2, final int id2) {
		final Relation relation = new Relation();
		relation.setDomainName(domainName);
		relation.setClass1Name(className1);
		relation.setCard1Id(id1);
		relation.setClass2Name(className2);
		relation.setCard2Id(id2);
		proxy.createRelation(relation);
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

	@Override
	public ReferenceType selectReference(String className, String attributeName, String attributeValue) {
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

	private ReferenceType referenceType(Card card) {
		final ReferenceType rt = new ReferenceType();
		rt.setId(card.getId());
		for (final Attribute attribute : card.getAttributeList()) {
			final String attributeName = attribute.getName();
			if (Constants.CLASS_ID_ATTRIBUTE.equals(attributeName)) {
				int classId = Integer.parseInt(attribute.getValue());
				rt.setIdClass(classId);
			} else if (Constants.DESCRIPTION_ATTRIBUTE.equals(attributeName)) {
				rt.setDescription(attribute.getValue());
			}
		}
		return rt;
	}

	public Query filterByAttribute(String attributeName, String attributeValue) {
		final Filter filter = new Filter();
		filter.setName(attributeName);
		filter.setOperator(SOAP_OPERATOR_EQUALS);
		filter.getValue().add(attributeValue);
		final Query query = new Query();
		query.setFilter(filter);
		return query;
	}

	/*
	 * SchemaApi
	 */

	@Override
	public ClassInfo findClass(final String className) {
		return schemaApi.findClass(className);
	}

	@Override
	public ClassInfo findClass(final int classId) {
		return schemaApi.findClass(classId);
	}

	@Override
	public LookupType selectLookupById(int id) {
		return schemaApi.selectLookupById(id);
	}

	@Override
	public LookupType selectLookupByCode(String type, String code) {
		return schemaApi.selectLookupByCode(type, code);
	}

	@Override
	public LookupType selectLookupByDescription(String type, String description) {
		return schemaApi.selectLookupByDescription(type, description);
	}
}
