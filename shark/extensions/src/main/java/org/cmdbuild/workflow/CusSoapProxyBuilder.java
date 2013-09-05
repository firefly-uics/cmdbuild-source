package org.cmdbuild.workflow;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.PasswordType;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.SoapClientBuilder;
import org.cmdbuild.services.soap.client.SoapClient;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class CusSoapProxyBuilder {

	private static final String CMDBUILD_WS_URL_PROPERTY = "org.cmdbuild.ws.url";
	private static final String CMDBUILD_WS_USERNAME_PROPERTY = "org.cmdbuild.ws.username";
	private static final String CMDBUILD_WS_PASSWORD_PROPERTY = "org.cmdbuild.ws.password";

	private static final String URL_SEPARATOR = "/";
	private static final String URL_SUFFIX = "services/soap/Private";

	private static final String USER_SEPARATOR = "#";

	private final CallbackUtilities cus;
	private String username;

	public CusSoapProxyBuilder(final CallbackUtilities cus) {
		this.cus = cus;
		this.username = StringUtils.EMPTY;
	}

	public CusSoapProxyBuilder withUsername(final String username) {
		Validate.notNull(username);
		this.username = username;
		return this;
	}

	public Private build() {
		final String url = completeUrl(cus.getProperty(CMDBUILD_WS_URL_PROPERTY));
		final String fullUsername = completeUsername(cus.getProperty(CMDBUILD_WS_USERNAME_PROPERTY), username);
		final String password = cus.getProperty(CMDBUILD_WS_PASSWORD_PROPERTY);

		final SoapClient<Private> soapClient = new SoapClientBuilder<Private>() //
				.forClass(Private.class) //
				.withUrl(url) //
				.withUsername(fullUsername) //
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

	private String completeUsername(final String wsUsername, final String currentUser) {
		return new StringBuilder(wsUsername) //
				.append(isNotBlank(currentUser) ? USER_SEPARATOR + currentUser : EMPTY) //
				.toString();
	}

}
