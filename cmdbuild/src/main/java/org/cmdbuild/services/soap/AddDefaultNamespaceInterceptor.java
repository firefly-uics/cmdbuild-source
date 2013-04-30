package org.cmdbuild.services.soap;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.cmdbuild.logger.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AddDefaultNamespaceInterceptor extends AbstractSoapInterceptor {

	private String defaultNS = "http://soap.services.cmdbuild.org";

    public AddDefaultNamespaceInterceptor() {
        super(Phase.USER_PROTOCOL);
    }

	@Override
	public void handleMessage(org.apache.cxf.binding.soap.SoapMessage cxfMessage) throws Fault {
		try {
			SOAPMessage soapMessage = cxfMessage.getContent(SOAPMessage.class);
			applyNamespaceWhenEmpty(soapMessage.getSOAPBody(), defaultNS);
		} catch (SOAPException e) {
			Log.SOAP.warn("Failed to add default namespace to the message body");
		}
	}

	private void applyNamespaceWhenEmpty(Node node, String ns) {
		applyNamespaceWhenEmptyRecursive(node.getOwnerDocument(), node, ns);
	}

	public static void applyNamespaceWhenEmptyRecursive(Document doc, Node node, String namespace) {
		if (node.getNodeType() == Node.ELEMENT_NODE && node.getNamespaceURI() == null) {
			doc.renameNode(node, namespace, node.getNodeName());
		}

		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); ++i) {
			applyNamespaceWhenEmptyRecursive(doc, list.item(i), namespace);
		}
	}

}
