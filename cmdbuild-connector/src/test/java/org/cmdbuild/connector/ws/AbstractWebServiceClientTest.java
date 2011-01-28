package org.cmdbuild.connector.ws;

import org.junit.Test;

public class AbstractWebServiceClientTest {

	private static final String URL = "url";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_Class() {
		new WebServiceClientImpl<String>(null, URL, USERNAME, PASSWORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_URL_Null() {
		new WebServiceClientImpl<String>(String.class, null, USERNAME, PASSWORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_URL_Empty() {
		new WebServiceClientImpl<String>(String.class, "", USERNAME, PASSWORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_Username() {
		new WebServiceClientImpl<String>(String.class, URL, null, PASSWORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_Password() {
		new WebServiceClientImpl<String>(String.class, URL, USERNAME, null);
	}

	private class WebServiceClientImpl<T> extends AbstractWebServiceClient<T> {

		public WebServiceClientImpl(final Class<T> proxyClass, final String url, final String username,
				final String password) {
			super(proxyClass, url, username, password);
		}

		@Override
		protected String getPasswordType() {
			return "";
		}

	}

}
