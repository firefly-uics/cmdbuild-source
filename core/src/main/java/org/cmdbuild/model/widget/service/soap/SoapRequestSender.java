package org.cmdbuild.model.widget.service.soap;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.lang.Validate;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.widget.service.soap.exception.ConnectionException;

public class SoapRequestSender {

	private final String endpointUrl;

	public SoapRequestSender(final String endpointUrl) {
		Validate.notNull(endpointUrl);
		Validate.notEmpty(endpointUrl);
		this.endpointUrl = endpointUrl;
	}

	public SOAPMessage send(final SoapRequest request) throws ConnectionException {
		SOAPConnection connection = null;
		try {
			final SOAPConnectionFactory connectionFactory = SOAPConnectionFactory.newInstance();
			connection = connectionFactory.createConnection();
			Log.CMDBUILD.info("Sending SOAP request to endpoint " + endpointUrl);
			final SOAPMessage response = connection.call(request.create(), endpointUrl);
			return response;
		} catch (final SOAPException ex) {
			Log.CMDBUILD.error(ex.getMessage());
			throw new ConnectionException("Message send failed. Possible causes: 1) The service is not deployed; \n "
					+ "2) The URL and/or the port number of the endpoint is not correct");
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (final SOAPException ex) {
					Log.CMDBUILD.error(ex.getMessage());
				}
			}
		}
	}

}
