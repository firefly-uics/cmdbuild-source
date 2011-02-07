package org.cmdbuild.portlet.ws;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.services.soap.Private;

public class SOAPClient {

	private final Private service;

	public SOAPClient(final String url, final String username, final String password) {
		Log.PORTLET.debug("Calling server at URL " + url + " with user " + username + " and password " + password);

		final JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
		proxyFactory.setServiceClass(Private.class);
		proxyFactory.setAddress(getEndpoint());
		service = (Private) proxyFactory.create();

		// do authentication here
		final Map<String, Object> outProps = new HashMap<String, Object>();
		final Client client = ClientProxy.getClient(service);
		final Endpoint cxfEndpoint = client.getEndpoint();
		// Manual WSS4JOutInterceptor interceptor process
		outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
		outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);
		final WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
		cxfEndpoint.getOutInterceptors().add(wssOut);
		outProps.put(WSHandlerConstants.USER, username);
		final ClientPasswordCallback pwdCallback = new ClientPasswordCallback(username, password);
		outProps.put(WSHandlerConstants.PW_CALLBACK_REF, pwdCallback);
	}

	private String getEndpoint() {
		return PortletConfiguration.getInstance().getCmdbuildUrl();
	}

	public Private getService() {
		return service;
	}

}
