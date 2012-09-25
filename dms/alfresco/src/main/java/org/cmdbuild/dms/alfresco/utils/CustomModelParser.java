package org.cmdbuild.dms.alfresco.utils;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Maps;

public class CustomModelParser {

	private static final String TYPE_NAMES_EXPRESSION = "/model/types/type/@name";
	private static final String ASPECT_NAMES_FOR_TYPE_EXPRESSION_FORMAT = "/model/types/type[@name='%s']/mandatory-aspects/aspect";
	private static final String PREFIX_NAME_SEPARATOR = ":";

	private final String content;
	private final String prefix;
	private Document document;

	public CustomModelParser(final String content, final String prefix) {
		this.content = content;
		this.prefix = prefix;
	}

	public Map<String, List<String>> getAspectsByType() {
		try {
			return unsafeAspectsByType();
		} catch (final Exception e) {
			// TODO log
			return Collections.emptyMap();
		}
	}

	private Map<String, List<String>> unsafeAspectsByType() throws Exception {
		final Map<String, List<String>> aspectsByType = Maps.newHashMap();
		parseContent();
		for (final String typeName : typeNames()) {
			aspectsByType.put(typeName, aspectsForType(typeName));
		}
		return aspectsByType;
	}

	private void parseContent() throws ParserConfigurationException, SAXException, IOException {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(new InputSource(new StringReader(content)));
	}

	private List<String> typeNames() throws XPathExpressionException {
		final List<String> typeNames = new ArrayList<String>();
		final XPathExpression expression = compileExpression(typeNamesExpression());
		final Object result = expression.evaluate(document, XPathConstants.NODESET);
		final NodeList typeNamesResults = (NodeList) result;
		for (int i = 0; i < typeNamesResults.getLength(); i++) {
			final String nodeValue = typeNamesResults.item(i).getNodeValue();
			typeNames.add(stripPrefixFromName(nodeValue));
		}
		return typeNames;
	}

	private List<String> aspectsForType(final String typeName) throws XPathExpressionException {
		final List<String> aspectNames = new ArrayList<String>();
		final XPathExpression expression = compileExpression(aspectNamesForTypeExpression(addPrefixToName(typeName)));
		final Object result = expression.evaluate(document, XPathConstants.NODESET);
		final NodeList typeNamesResults = (NodeList) result;
		for (int i = 0; i < typeNamesResults.getLength(); i++) {
			final String nodeValue = typeNamesResults.item(i).getTextContent();
			aspectNames.add(stripPrefixFromName(nodeValue));
		}
		return aspectNames;
	}

	private String stripPrefixFromName(final String name) {
		return name.replaceAll(format("%s%s", prefix, PREFIX_NAME_SEPARATOR), EMPTY);
	}

	private String addPrefixToName(final String name) {
		return format("%s%s%s", prefix, PREFIX_NAME_SEPARATOR, name);
	}

	private static XPathExpression compileExpression(final String expression) throws XPathExpressionException {
		final XPathFactory xFactory = XPathFactory.newInstance();
		final XPath xpath = xFactory.newXPath();
		final XPathExpression typeNamesExpression = xpath.compile(expression);
		return typeNamesExpression;
	}

	private static String typeNamesExpression() {
		return TYPE_NAMES_EXPRESSION;
	}

	private static String aspectNamesForTypeExpression(final String typeName) {
		return format(ASPECT_NAMES_FOR_TYPE_EXPRESSION_FORMAT, typeName);
	}

}