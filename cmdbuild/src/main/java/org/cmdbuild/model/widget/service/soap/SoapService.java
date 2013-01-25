package org.cmdbuild.model.widget.service.soap;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.lang.Validate;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.widget.service.ExternalService;
import org.cmdbuild.model.widget.service.soap.SoapRequest.SoapRequestBuilder;
import org.w3c.dom.Document;

public class SoapService implements ExternalService {

	public static class SoapServiceBuilder {

		private SoapRequestBuilder requestBuilder;
		private SoapRequestSender requestSender;

		private SoapServiceBuilder() {
			requestBuilder = SoapRequest.newSoapRequest();
		}

		/**
		 * Note that it must include also the port if it differs from 80
		 */
		public SoapServiceBuilder withEndpointUrl(String endpointUrl) {
			requestSender = new SoapRequestSender(endpointUrl);
			return this;
		}

		public SoapServiceBuilder withNamespacePrefix(String prefix) {
			requestBuilder.withNamespacePrefix(prefix);
			return this;
		}

		public SoapServiceBuilder withNamespaceUri(String uri) {
			requestBuilder.withNamespaceUri(uri);
			return this;
		}

		public SoapServiceBuilder callingMethod(String methodName) {
			requestBuilder.callingMethod(methodName);
			return this;
		}

		public SoapServiceBuilder withParameters(Map<String, String> params) {
			requestBuilder.withParameters(params);
			return this;
		}

		public SoapServiceBuilder withParameter(String name, String value) {
			return this;
		}

		public SoapService build() {
			SoapRequest request = requestBuilder.build();
			Validate.notNull(requestSender);
			return new SoapService(requestSender, request);
		}

	}

	private SoapRequestSender sender;
	private SoapRequest request;

	private SoapService(SoapRequestSender sender, SoapRequest request) {
		this.sender = sender;
		this.request = request;
	}

	@Override
	public Document invoke() {
		try {
			SOAPMessage response = sender.send(request);
			SOAPBody responseBody = response.getSOAPBody();
			Document document = responseBody.extractContentAsDocument();
			return document;
		} catch (SOAPException ex) {
			Log.OTHER.error(ex.getMessage());
			return createNewEmptyDocument();
		}
	}

	private Document createNewEmptyDocument() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (ParserConfigurationException e) {
			Log.OTHER.warn("Cannot create an empty Document response");
			return null;
		}
	}

	public static SoapServiceBuilder newSoapService() {
		return new SoapServiceBuilder();
	}

}
