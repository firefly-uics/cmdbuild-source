package org.cmdbuild.model.widget.service.soap;

import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Maps;

public class SoapRequest {

	public static class SoapRequestBuilder {

		private String namespacePrefix;
		private String namespaceUri;
		private String methodName;
		private Map<String, String> parameters;

		private SoapRequestBuilder() {
			parameters = Maps.newHashMap();
		}

		public SoapRequestBuilder withNamespacePrefix(String prefix) {
			this.namespacePrefix = prefix;
			return this;
		}

		public SoapRequestBuilder withNamespaceUri(String uri) {
			this.namespaceUri = uri;
			return this;
		}

		public SoapRequestBuilder callingMethod(String methodName) {
			this.methodName = methodName;
			return this;
		}

		public SoapRequestBuilder withParameters(Map<String, String> parameters) {
			this.parameters.putAll(parameters);
			return this;
		}

		public SoapRequestBuilder withParameter(String name, String value) {
			this.parameters.put(name, value);
			return this;
		}

		public SoapRequest build() {
			Validate.notNull(methodName);
			Validate.notEmpty(methodName);
			validateNamespaceUriAndPrefix();
			return new SoapRequest(this);
		}

		private void validateNamespaceUriAndPrefix() {
			if (namespacePrefix != null && (namespaceUri == null || namespaceUri.isEmpty())) {
				throwException();
			}
		}

		private void throwException() {
			throw new IllegalArgumentException(
					"When you specify the namespace prefix, you have also to specify the uri");
		}

	}

	private String namespacePrefix;
	private String namespaceUri;
	private String methodToBeCalled;
	private Map<String, String> parameters;

	private SoapRequest(SoapRequestBuilder builder) {
		this.methodToBeCalled = builder.methodName;
		this.parameters = builder.parameters;
		this.namespacePrefix = builder.namespacePrefix;
		this.namespaceUri = builder.namespaceUri;
	}

	public SOAPMessage create() throws SOAPException {
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage message = messageFactory.createMessage();
		SOAPPart part = message.getSOAPPart();
		SOAPEnvelope envelope = part.getEnvelope();
		SOAPBody body = envelope.getBody();
		createBodyWithMethodAndParameters(body);
		message.saveChanges();
		return message;
	}

	private void createBodyWithMethodAndParameters(SOAPBody body) throws SOAPException {
		SOAPElement method = body.addChildElement(methodToBeCalled, namespacePrefix, namespaceUri);
		for (String paramName : parameters.keySet()) {
			String value = parameters.get(paramName);
			SOAPElement paramTag = method.addChildElement(paramName);
			paramTag.addTextNode(value);
		}
	}

	public static SoapRequestBuilder newSoapRequest() {
		return new SoapRequestBuilder();
	}

}
