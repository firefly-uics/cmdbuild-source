package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.configuration.GridConfiguration;
import org.cmdbuild.portlet.layout.ComponentLayout;
import org.cmdbuild.portlet.layout.PortletLayout;
import org.cmdbuild.portlet.operation.ReferenceOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.utils.GridUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.services.soap.Reference;
import org.cmdbuild.servlet.util.SessionAttributes;

public class ReferenceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		final ServletOperation operations = new ServletOperation();
		response.setContentType("text/xml");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		final PrintWriter out = response.getWriter();
		final HttpSession session = request.getSession();
		final GridUtils gridUtils = new GridUtils();
		final GridConfiguration gridConfig = gridUtils.getGridConfiguration(request);
		final CardUtils cardUtils = new CardUtils();
		final CardConfiguration cardConfig = cardUtils.getCardConfiguration(request);
		final SOAPClient client = operations.getClient(session);
		final String email = StringUtils.defaultIfEmpty((String) session.getAttribute(SessionAttributes.EMAIL),
				StringUtils.EMPTY);
		final PortletLayout portletLayout = new PortletLayout(client, email, request.getContextPath());
		final ComponentLayout layout = portletLayout.serializeComponent(cardConfig.getClassname(),
				generateAttributeSchemaForReference(cardConfig), null, null, null, true);
		try {
			out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			out.println("<rows>");
			out.println("<page>" + gridConfig.getPage() + "</page>");
			final List<Reference> references = getCardInfo(portletLayout, layout, gridConfig);
			if (references != null && references.size() > 0) {
				out.println(getXMLString(references));
			} else {
				out.println("<total>0</total>\n");
			}
			out.println("</rows>");
		} finally {
			response.getWriter().flush();
			response.getWriter().close();
		}
	}

	private AttributeSchema generateAttributeSchemaForReference(final CardConfiguration cardConfig) {
		final AttributeSchema schema = new AttributeSchema();
		schema.setReferencedClassName(cardConfig.getClassname());
		return schema;
	}

	private String getXMLString(final List<Reference> list) {

		final Reference c = list.get(0);
		final int total = c.getTotalRows();
		String result = "<total>" + total + "</total>";
		for (int i = 0; i < list.size(); i++) {
			final Reference reference = list.get(i);
			final StringBuffer row = new StringBuffer();
			row.append(String.format("<row id=\'%d\'>\n", reference.getId())).append(
					String.format("<cell><![CDATA[%d]]></cell>\n", reference.getId())).append(
					String.format("<cell><![CDATA[%s]]></cell>\n", reference.getDescription())).append("</row>\n");

			result = result + row.toString();
		}

		return result;
	}

	private List<Reference> getCardInfo(final PortletLayout portletLayout, final ComponentLayout layout,
			final GridConfiguration config) {
		final ReferenceOperation operation = new ReferenceOperation(portletLayout.getClient());
		final Order order = new Order();
		order.setColumnName(StringUtils.defaultIfEmpty(config.getSortname(), "Description"));
		if (config.getSortorder().equalsIgnoreCase("ASC")) {
			order.setType("ASC");
		} else {
			order.setType("DESC");
		}
		final List<Order> orders = new ArrayList<Order>();
		orders.add(order);
		Query q = null;
		if (config.getFullTextQuery() != null && config.getQuery() != null && !(config.getFullTextQuery().equals(""))) {
			q = new Query();
			final Filter filter = new Filter();
			filter.setName(config.getQuery());
			filter.setOperator("LIKE");
			filter.getValue().add("%" + config.getFullTextQuery() + "%");
			q.setFilter(filter);
		}
		return operation.getReferenceList(layout, q, orders, config.getMaxResult(), config.getStartIndex());
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		processRequest(req, resp);
	}
}
