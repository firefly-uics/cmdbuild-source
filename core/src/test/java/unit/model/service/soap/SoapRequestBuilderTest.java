package unit.model.service.soap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.cmdbuild.model.widget.service.soap.SoapRequest;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;

public class SoapRequestBuilderTest {

	private static final String PREFIX = "it";
	private static final String URI = "http://it.example";
	private static final String METHOD_NAME = "method";
	private Map<String, String> expectedParams = Maps.newHashMap();
	private static final String FIRST_PARAM_NAME = "first";
	private static final String SECOND_PARAM_NAME = "second";
	private static final String THIRD_PARAM_NAME = "third";
	
	@Before
	public void setUp() {
		expectedParams.put(FIRST_PARAM_NAME, "value1");
		expectedParams.put(SECOND_PARAM_NAME, "value2");
		expectedParams.put(THIRD_PARAM_NAME, "value3");
	}

	@Test
	public void shouldCreateRequestWithoutParameters() throws Exception {
		// given
		SoapRequest request = SoapRequest.newSoapRequest() //
				.withNamespacePrefix(PREFIX) //
				.withNamespaceUri(URI) //
				.callingMethod(METHOD_NAME) //
				.build();

		// when
		SOAPMessage message = request.create();
		
		// then
		SOAPBody body = message.getSOAPBody();
		Document document = body.extractContentAsDocument();
		Node root = getDocumentRoot(document);
		assertEquals(0, root.getChildNodes().getLength());
	}
	
	@Test
	public void shouldCreateRequestWithoutNamespaceUriAndPrefix() throws Exception {
		// given
		SoapRequest request = SoapRequest.newSoapRequest() //
				.callingMethod(METHOD_NAME) //
				.build();
		
		// when
		SOAPMessage message = request.create();
		
		// then
		SOAPBody body = message.getSOAPBody();
		Document document = body.extractContentAsDocument();
		Node methodTag = getDocumentRoot(document);
		assertEquals(METHOD_NAME, methodTag.getNodeName());
		assertNull(methodTag.getNamespaceURI());
		assertNull(methodTag.getPrefix());
	}
	
	@Test
	public void shouldCreateRequestWithParametersValue() throws Exception {
		// given
		Map<String, String> params = Maps.newHashMap();
		params.put(SECOND_PARAM_NAME, "2nd_param");
		params.put(THIRD_PARAM_NAME, "3rd_param");
		SoapRequest request = SoapRequest.newSoapRequest() //
				.callingMethod(METHOD_NAME) //
				.withParameter(FIRST_PARAM_NAME, "18") //
				.withParameters(params) //
				.build();
		
		// when
		SOAPMessage message = request.create();
		SOAPBody body = message.getSOAPBody();
		Document document = body.extractContentAsDocument();
		Node methodTag = getDocumentRoot(document);
		NodeList parameters = methodTag.getChildNodes();
		
		// then
		assertEquals(3, methodTag.getChildNodes().getLength());
		assertEquals(METHOD_NAME, methodTag.getNodeName());
		for (int i = 0; i < parameters.getLength(); i++) {
			assertTrue(expectedParams.containsKey(parameters.item(i).getNodeName()));
		}
	}
	
	@Test
	public void shouldCreateRequestWithCorrectNamespaceUriAndPrefx() throws Exception {
		// given
		SoapRequest request = SoapRequest.newSoapRequest() //
				.withNamespacePrefix(PREFIX) //
				.withNamespaceUri(URI) //
				.callingMethod(METHOD_NAME) //
				.build();
		
		// when
		SOAPMessage message = request.create();
		message.writeTo(System.out);
		SOAPBody body = message.getSOAPBody();
		Document document = body.extractContentAsDocument();
		Node methodTag = getDocumentRoot(document);
		
		// then
		assertEquals(URI, methodTag.getNamespaceURI());
		assertEquals(PREFIX, methodTag.getPrefix());
	}
	
	private Node getDocumentRoot(Document document) {
		NodeList nodeList = document.getChildNodes();
		if (nodeList.getLength() == 1) {
			return nodeList.item(0);
		}
		fail("There is more than one root or the document does not have a root");
		return null;
	}
	
}
