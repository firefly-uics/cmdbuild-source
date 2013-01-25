package org.cmdbuild.model.widget.service.soap;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.lang.Validate;
import org.cmdbuild.logger.Log;

public class SoapRequestSender {

	private String endpointUrl;

	public SoapRequestSender(String endpointUrl) {
		Validate.notNull(endpointUrl);
		Validate.notEmpty(endpointUrl);
		this.endpointUrl = endpointUrl;
	}

	public SOAPMessage send(SoapRequest request) throws SOAPException {
		SOAPConnection connection = null;
		try {
			SOAPConnectionFactory connectionFactory = SOAPConnectionFactory.newInstance();
			connection = connectionFactory.createConnection();
			Log.OTHER.info("Sending SOAP request to endpoint " + endpointUrl);
			SOAPMessage response = connection.call(request.create(), endpointUrl);
			return response;
		} catch (SOAPException ex) {
			Log.OTHER.error(ex.getMessage());
			throw ex;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SOAPException ex) {
					Log.OTHER.error(ex.getMessage());
				}
			}
		}
	}

}
