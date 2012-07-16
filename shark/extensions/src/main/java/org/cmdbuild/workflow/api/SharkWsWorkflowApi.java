package org.cmdbuild.workflow.api;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Relation;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.PasswordType;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.SoapClientBuilder;
import org.cmdbuild.services.soap.client.SoapClient;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class SharkWsWorkflowApi extends SharkWorkflowApi {

	private static SchemaApi NULL_SCHEMA_API = new SchemaApi() {

		@Override
		public ClassInfo findClass(final String className) {
			throw new UnsupportedOperationException("Not yet configured");
		}

		@Override
		public ClassInfo findClass(final int classId) {
			throw new UnsupportedOperationException("Not yet configured");
		}

	};

	private static final String CMDBUILD_WS_URL_PROPERTY = "org.cmdbuild.ws.url";
	private static final String CMDBUILD_WS_USERNAME_PROPERTY = "org.cmdbuild.ws.username";
	private static final String CMDBUILD_WS_PASSWORD_PROPERTY = "org.cmdbuild.ws.password";

	private static final String URL_SEPARATOR = "/";
	private static final String URL_SUFFIX = "services/soap/Private";

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

}
