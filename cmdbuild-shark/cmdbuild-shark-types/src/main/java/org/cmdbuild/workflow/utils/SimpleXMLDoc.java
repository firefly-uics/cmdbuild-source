package org.cmdbuild.workflow.utils;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Really deadly simple xml document producer
 */
public class SimpleXMLDoc{

	private SimpleXMLNode root;
	
	public SimpleXMLDoc(String rootName){
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.newDocument();
			root = new SimpleXMLNode(document, rootName);
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();//FIXME
		}
	}
	
	public SimpleXMLNode getRoot(){ 
		return root; 
	}
	
	public String write(){
		try {
			Source source = new DOMSource(root.element());
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			
			return stringWriter.getBuffer().toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace(); //FIXME
		} catch (TransformerException e) {
			e.printStackTrace();//FIXME
		}
		return null;
	}
	
	public String toString() {
		return write();
	}
}
