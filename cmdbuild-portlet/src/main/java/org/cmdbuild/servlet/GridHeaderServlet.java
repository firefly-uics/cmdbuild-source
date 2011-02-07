package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.metadata.CMDBuildTagDictionary;
import org.cmdbuild.portlet.operation.GridOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.operation.WSOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Metadata;

public class GridHeaderServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		final ServletOperation operations = new ServletOperation();
		final SOAPClient client = operations.getClient(request.getSession());
		final WSOperation operation = new WSOperation(client);
		final GridOperation goperation = new GridOperation(request.getContextPath());
		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		String colModel = "";
		final CardUtils cardUtils = new CardUtils();
		final CardConfiguration cardConfig = cardUtils.getCardConfiguration(request);
		final List<AttributeSchema> schema = operation.getAttributeList(cardConfig.getClassname());
		if (schema != null) {
			for (final AttributeSchema s : schema) {
				setMetadataSessionVariable(s, request.getSession());
			}
		}
		final String index = "";

		colModel = "{display:\'ID\', name:\'id\', width:10, fixed: true, hide: true}";
		for (final AttributeSchema as : schema) {
			colModel = goperation.generateGridHeaders(as, colModel, index);
		}
		colModel = goperation.generateButtonHeaders(colModel);
		out.write("[" + colModel + "]");
		out.flush();
		out.close();
	}

	private void setMetadataSessionVariable(final AttributeSchema as, final HttpSession session) {
		final List<Metadata> metadata = as.getMetadata();
		if (metadata != null) {
			for (final Metadata meta : metadata) {
				if (meta.getKey().equals(CMDBuildTagDictionary.USERID)) {
					Log.PORTLET.debug("Setting session userid from metadata");
					session.setAttribute("cmdbmeta.userid", as.getName());
				}
			}
		}
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
