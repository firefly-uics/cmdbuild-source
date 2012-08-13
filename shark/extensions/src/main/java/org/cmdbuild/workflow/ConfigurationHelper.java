package org.cmdbuild.workflow;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;

import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.PasswordType;
import org.cmdbuild.services.soap.client.CmdbuildSoapClient.SoapClientBuilder;
import org.cmdbuild.services.soap.client.SoapClient;
import org.cmdbuild.workflow.api.SharkWorkflowApi;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class ConfigurationHelper {

	private static final String CMDBUILD_API_CLASSNAME_PROPERTY = "org.cmdbuild.workflow.api.classname";

	private static final String CMDBUILD_WS_URL_PROPERTY = "org.cmdbuild.ws.url";
	private static final String CMDBUILD_WS_USERNAME_PROPERTY = "org.cmdbuild.ws.username";
	private static final String CMDBUILD_WS_PASSWORD_PROPERTY = "org.cmdbuild.ws.password";

	private static final String URL_SEPARATOR = "/";
	private static final String URL_SUFFIX = "services/soap/Private";

	private final CallbackUtilities cus;

	public ConfigurationHelper(final CallbackUtilities cus) {
		this.cus = cus;
	}

	public SharkWorkflowApi newSharkWorkflowApi() throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		final String classname = cus.getProperty(CMDBUILD_API_CLASSNAME_PROPERTY);
		cus.info(null, format("loading api '%s'", classname));
		final Class<? extends SharkWorkflowApi> sharkWorkflowApiClass = Class.forName(classname).asSubclass(
				SharkWorkflowApi.class);
		final SharkWorkflowApi sharkWorkflowApi = sharkWorkflowApiClass.newInstance();
		sharkWorkflowApi.configure(cus);
		return sharkWorkflowApi;
	}

	public Private newProxy() {
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

}
