package org.cmdbuild.workflow.api;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.Map;

import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.PasswordType;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.SoapClientBuilder;
import org.cmdbuild.services.soap.client.SoapClient;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class SharkWsWorkflowApi extends SharkWorkflowApi {

	private static final String URL_SEPARATOR = "/";
	private static final String URL_SUFFIX = "services/soap/Private";

	private Private proxy;

	@Override
	public void configure(final CallbackUtilities cus) {
		super.configure(cus);
		configureProxy(cus);
	}

	private void configureProxy(final CallbackUtilities cus) {
		final String base_url = cus.getProperty("org.cmdbuild.ws.url");
		final String url = completeUrl(base_url);
		final String username = cus.getProperty("org.cmdbuild.ws.username");
		final String password = cus.getProperty("org.cmdbuild.ws.password");

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
		proxy = soapClient.getProxy();
	}

	private String completeUrl(final String baseUrl) {
		return new StringBuilder(baseUrl) //
				.append(baseUrl.endsWith(URL_SEPARATOR) ? EMPTY : URL_SEPARATOR) //
				.append(URL_SUFFIX) //
				.toString();
	}

	public Private getProxy() {
		return proxy;
	}

	public void setProxy(final Private proxy) {
		this.proxy = proxy;
	}

	@Override
	public int createCard(final String classname, final Map<String, String> attributes) {
		final Card card = new Card();
		card.setClassName(classname);
		for (final String name : attributes.keySet()) {
			card.getAttributeList().add(attribute(name, attributes.get(name)));
		}
		final int id = proxy.createCard(card);
		return id;
	}

	private Attribute attribute(final String name, final String value) {
		final Attribute attribute = new Attribute();
		attribute.setName(name);
		attribute.setValue(value);
		return attribute;
	}

}
