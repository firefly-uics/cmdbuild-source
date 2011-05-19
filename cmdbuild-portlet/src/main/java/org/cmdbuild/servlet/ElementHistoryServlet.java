package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.servlet.util.SessionAttributes;

public class ElementHistoryServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String CLASSNAME = "classname";
	private static final String TYPE = "type";
	private static final String CARDID = "cardid";
	private static final String PAGE = "page";
	private static final String MAXRESULT = "rp";
	private String contextPath;

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final ServletOperation operations = new ServletOperation();
		final HttpSession session = request.getSession(true);
		final SOAPClient client = operations.getClient(session);
		this.contextPath = (String) session.getAttribute(SessionAttributes.CONTEXT_PATH);
		final String classname = request.getParameter(CLASSNAME);
		final String type = request.getParameter(TYPE);
		final int cardid = Integer.parseInt(request.getParameter(CARDID));
		final String page = StringUtils.defaultIfEmpty(request.getParameter(PAGE), "1");
		final String maxResult = StringUtils.defaultIfEmpty(request.getParameter(MAXRESULT), "10");

		response.setContentType("text/xml");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");

		final String pageString = StringUtils.defaultIfEmpty(page, "1");
		final int ipage = Integer.parseInt(pageString);
		final String limitString = StringUtils.defaultIfEmpty(maxResult, "10");
		final int rpage = Integer.parseInt(limitString);
		final int startIndex = (ipage - 1) * rpage;

		final PrintWriter out = response.getWriter();
		out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		out.println("<rows>");
		out.println("<page>" + ipage + "</page>");
		try {
			final CardList cardlist = getCardInfo(client, classname, cardid, startIndex, rpage);
			if (cardlist != null && cardlist.getCards().size() > 0) {
				out.println(getXMLString(cardlist, type));
			} else {
				out.println("<total>0</total>\n");
			}
			out.println("</rows>");
		} finally {
			out.flush();
			out.close();
		}
	}

	private CardList getCardInfo(final SOAPClient client, final String classname, final int cardid,
			final int startIndex, final int maxResult) {
		final CardOperation operation = new CardOperation(client);
		final CardList cards = operation.getCardHistory(classname, cardid, maxResult, startIndex);
		return cards;
	}

	private String getXMLString(final CardList list, final String type) {

		String result = "";
		if (list != null) {
			final int total = list.getTotalRows();
			result = "<total>" + total + "</total>\n";

			for (int i = 0; i < list.getCards().size(); i++) {
				final Card card = list.getCards().get(i);
				final List<Attribute> attrs = card.getAttributeList();
				final StringBuilder row = new StringBuilder();
				row.append(String.format("<row id='%d'>\n", card.getId()));
				row.append(String.format("<cell><![CDATA[%d]]></cell>\n", card.getId()));
				row.append(generateCell(attrs, "BeginDate"));
				row.append(generateEndDateCell(attrs, "EndDate"));
				row.append(generateCell(attrs, "Description"));
				row.append(String.format("<cell><![CDATA[<img src=\"%s/css/images/zoom.png\" alt=\"Storia\" "
						+ "onclick=\"CMDBuildShowElementDetail('%s', '%s', '%d', '%s')\" />]]></cell>\n", contextPath,
						i, card.getClassName(), card.getId(), type));
				row.append("</row>\n");
				result = result + row.toString();
			}
		} else {
			final StringBuilder row = new StringBuilder();
			row.append(String.format("<total>%s</total>\n", 0));
			row.append(String.format("<row id='%s'>\n", 0));
			row.append("</row>\n");
			result = result + row.toString();
		}
		return result;
	}

	private String generateCell(final List<Attribute> attrs, final String value) {
		String result = "";
		if (attrs != null && attrs.size() > 0) {
			for (final Attribute attribute : attrs) {
				final String aname = attribute.getName();
				final String avalue = attribute.getValue();
				if (aname.equals(value)) {
					result = result + "<cell><![CDATA[" + avalue + "]]></cell>\n";
				}
			}
		}
		return result;
	}

	private String generateEndDateCell(final List<Attribute> attrs, final String string) {
		String result = "";
		String endDateValue = "";
		boolean isEndDate = false;
		for (final Attribute a : attrs) {
			if (a.getName().equals("EndDate")) {
				isEndDate = true;
				endDateValue = a.getValue();
			}
		}
		if (isEndDate) {
			result = result + "<cell><![CDATA[" + endDateValue + "]]></cell>\n";
		} else {
			result = result + "<cell></cell>\n";
		}
		return result;
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
