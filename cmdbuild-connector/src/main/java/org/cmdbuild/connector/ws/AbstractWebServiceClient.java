package org.cmdbuild.connector.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.lang.Validate;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;

public abstract class AbstractWebServiceClient<T> {

	private final Class<T> proxyClass;
	private final String url;
	private final String username;
	private final String password;
	private T proxy;

	public AbstractWebServiceClient(final Class<T> proxyClass, final String url, final String username,
			final String password) {
		Validate.notNull(proxyClass, "null class");
		Validate.notNull(url, "null url");
		Validate.notEmpty(url, "empty url");
		Validate.notNull(username, "null username");
		Validate.notNull(password, "null password");
		this.proxyClass = proxyClass;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	protected abstract String getPasswordType();

	public T getProxy() {
		if (proxy == null) {
			proxy = createProxy();
		}
		return proxy;
	}

	@SuppressWarnings("unchecked")
	private T createProxy() {
		final JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
		proxyFactory.setServiceClass(proxyClass);
		// TODO proxyFactory.setWsdlLocation()
		proxyFactory.setAddress(url);
		final Object proxy = proxyFactory.create();

		final Map<String, Object> outProps = new HashMap<String, Object>();
		final String passwordType = getPasswordType();
		if (!WSConstants.PW_NONE.equals(passwordType)) {
			outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
			outProps.put(WSHandlerConstants.PASSWORD_TYPE, passwordType);
			outProps.put(WSHandlerConstants.USER, username);
			outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new ClientPasswordCallback(username, password));
		}

		final Client client = ClientProxy.getClient(proxy);
		final Endpoint cxfEndpoint = client.getEndpoint();
		cxfEndpoint.getOutInterceptors().add(new WSS4JOutInterceptor(outProps));

		return (T) proxy;
	}

	private final class ClientPasswordCallback implements CallbackHandler {

		private final String password;
		private final String username;

		public ClientPasswordCallback(final String username, final String password) {
			this.username = username;
			this.password = password;
		}

		public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
			final WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
			if (username.equals(pc.getIdentifier())) {
				pc.setPassword(password);
			}
		}
	}

}
