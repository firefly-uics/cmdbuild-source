package org.cmdbuild.service.rest.test;

import org.apache.commons.httpclient.NameValuePair;

public class HttpClientUtils {

	public static NameValuePair param(final String name, final String value) {
		return new NameValuePair(name, value);
	}

	public static NameValuePair[] all(final NameValuePair... params) {
		return params;
	}

	private HttpClientUtils() {
		// prevents instantiation
	}

}
