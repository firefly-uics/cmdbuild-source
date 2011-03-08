package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.configuration.GridConfiguration;
import org.cmdbuild.portlet.configuration.LinkCardItem;
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.operation.GridOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.utils.CQLUtils;
import org.cmdbuild.portlet.utils.GridUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;

public class LinkCardServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String OPERATION_PARAMETER = "operation";
	private static final String IDENTIFIER = "identifier";
	private static final String CQLQUERY_OBJECT = "CQLQuery";
	private static final String HEADER = "header";
	private static final String DATA = "data";
	private static final String DEFAULTSELECTED = "defaultSelected";
	private final ServletOperation operations = new ServletOperation();
	private final GridUtils gridUtils = new GridUtils();
	private final CQLUtils cqlUtils = new CQLUtils();

	private String getData(final HttpServletRequest request, final HttpServletResponse response) {
		response.setContentType("text/xml");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		final SOAPClient client = operations.getClient(request.getSession());
		final GridOperation gridOperation = new GridOperation(request.getContextPath());
		final String identifier = request.getParameter(IDENTIFIER);
		final GridConfiguration gridConfig = gridUtils.getGridConfiguration(request);
		final CardOperation cardOperation = new CardOperation(client);
		final LinkCardItem item = (LinkCardItem) request.getSession().getAttribute(identifier);
		final StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>").append("<rows>");
		final CqlQuery query = (CqlQuery) request.getSession().getAttribute(CQLQUERY_OBJECT);
		if (item.getClassname() != null && (item.getFilter() == null || query != null)) {
			final CardConfiguration cardConfig = new CardConfiguration();
			cardConfig.setClassname(item.getClassname());
			final CardList cardList = gridOperation.getCardInfo(cardConfig, gridConfig, query);
			final List<AttributeSchema> schema = cardOperation.getAttributeList(item.getClassname());
			xml.append("<page>" + gridConfig.getPage() + "</page>");
			if (schema != null) {
				xml.append(getXML(cardList, schema, item, gridOperation, cardOperation, request.getContextPath()));
			} else {
				xml.append("<total>0</total>\n");
			}
			xml.append("</rows>");
			request.getSession().removeAttribute(CQLQUERY_OBJECT);
		} else {
			xml.append("<page>0</page>");
			xml.append("<total>0</total>\n");
			xml.append("</rows>");
			request.getSession().removeAttribute(CQLQUERY_OBJECT);
		}
		return xml.toString();
	}

	private String getDefaultSelected(final HttpServletRequest request) {
		final SOAPClient client = operations.getClient(request.getSession());
		final CardOperation cardOperation = new CardOperation(client);
		final String identifier = request.getParameter(IDENTIFIER);
		final CqlQuery query = cqlUtils.getCQLQuery(request);
		final CardList cardList = cardOperation.getCardList(null, null, null, null, 0, 0, "", query);
		final StringBuffer result = new StringBuffer();
		for (final Card card : cardList.getCards()) {
			final String image = String
					.format(
							"<img src='%s/css/images/cross.png' class='CMDBuildLinkCardItemButton' onclick=\"CMDBuildRemoveItem(this, '%s', '%s', '%s')\" />",
							request.getContextPath(), identifier, card.getId(), cardOperation.getAttributeFromCard(
									card, "Description").getValue());
			final String inputField = String.format("<input type='hidden' name='hiddenLinkCard_%s' value='%s'/>",
					String.valueOf(card.getId()), String.valueOf(card.getId()));
			final StringBuffer row = new StringBuffer();
			row.append(String.format("<div class='row_%s CMDBuildLinkCardItem'>", card.getId()));
			row.append(image);
			row.append(String.format("<div class='CMDBuildLinkCardItemDescription'>%s</div>", cardOperation
					.getAttributeFromCard(card, "Description").getValue()));
			row.append(inputField);
			row.append("</div>");
			result.append(row.toString());
		}
		return result.toString();
	}

	private String getHeader(final HttpServletRequest request, final HttpServletResponse response) {
		final SOAPClient client = operations.getClient(request.getSession());
		final CardOperation cardOperation = new CardOperation(client);
		final GridOperation gridOperation = new GridOperation(request.getContextPath());
		final String identifier = request.getParameter(IDENTIFIER);
		final LinkCardItem item = (LinkCardItem) request.getSession().getAttribute(identifier);
		if (item.getClassname() != null) {
			final List<AttributeSchema> schema = cardOperation.getAttributeList(item.getClassname());
			final CqlQuery query = cqlUtils.getCQLQuery(request);
			request.getSession().setAttribute(CQLQUERY_OBJECT, query);
			response.setContentType("text/html;charset=UTF-8");
			final String index = "";
			String colModel = "{display:\'ID\', name:\'id\', width:10, fixed: true, hide: true}";
			for (final AttributeSchema as : schema) {
				colModel = gridOperation.generateGridHeaders(as, colModel, index);
			}
			colModel = colModel + "," + addLinkCardButtonArea();
			return "[" + colModel + "]";
		} else {
			return "[]";
		}
	}

	private void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final PrintWriter out = response.getWriter();
		if (request.getParameter(OPERATION_PARAMETER).equals(HEADER)) {
			out.write(getHeader(request, response));
		} else if (request.getParameter(OPERATION_PARAMETER).equals(DATA)) {
			out.write(getData(request, response));
		} else if (request.getParameter(OPERATION_PARAMETER).equals(DEFAULTSELECTED)) {
			out.write(getDefaultSelected(request));
		}
		out.flush();
		out.close();
	}

	private String addLinkCardButtonArea() {
		return String.format("{display: \'\', name: \'\', width:%d, fixed: true, sortable: false}", 30);
	}

	private String getXML(final CardList list, final List<AttributeSchema> schema, final LinkCardItem item,
			final GridOperation gridOperation, final CardOperation cardOperation, final String contextPath) {
		String result = "";
		if (list != null) {
			final int total = list.getTotalRows();
			result = "<total>" + total + "</total>\n";
			for (final Card card : list.getCards()) {
				final List<Attribute> attrs = card.getAttributeList();
				final StringBuilder cardBuilder = new StringBuilder();
				cardBuilder.append("<row id=\'").append(card.getId()).append("\'>\n");
				cardBuilder.append("<cell><![CDATA[").append(card.getId()).append("]]></cell>\n");
				cardBuilder.append(gridOperation.serializeCell(attrs, schema));
				cardBuilder.append(serializeButtonCell(card, cardOperation, item, contextPath));
				cardBuilder.append("</row>\n");
				result = result + cardBuilder.toString();
			}
		} else {
			result = "<total>" + 0 + "</total>\n";
			result = result + "<row id=\'" + 0 + "\'>\n";
			result = result + "</row>\n";
		}
		return result;
	}

	private String serializeButtonCell(final Card card, final CardOperation cardOperation, final LinkCardItem item,
			final String contextPath) {
		final String description = cardOperation.getAttributeFromCard(card, "Description").getValue();
		final String linkCardButtonClass = "linkCardButton_" + card.getId();
		if (item.getSingleSelect() > 0) {
			return String
					.format(
							"<cell><![CDATA[<img src=\"%s/css/images/add.png\" class=\"CMDBuildGridButton %s\" onclick=\"CMDBuildSelectLinkCard(this, '%s', '%s', 'radio')\"/>]]></cell>",
							contextPath, linkCardButtonClass, item.getIdentifier(), description);
		} else if (item.getNoSelect() > 0) {
			return "<cell><![CDATA[]]></cell>";
		} else {
			return String
					.format(
							"<cell><![CDATA[<img src=\"%s/css/images/add.png\" class=\"CMDBuildGridButton %s\" onclick=\"CMDBuildSelectLinkCard(this, '%s', '%s', 'checkbox')\"/>]]></cell>",
							contextPath, linkCardButtonClass, item.getIdentifier(), description);
		}
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
			IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}
}
