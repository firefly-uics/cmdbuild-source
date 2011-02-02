package org.cmdbuild.servlet;

import org.cmdbuild.portlet.operation.ServletOperation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.configuration.GridConfiguration;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.metadata.CMDBuildTagDictionary;
import org.cmdbuild.portlet.operation.GridOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.utils.GridUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.portlet.operation.WSOperation;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Metadata;

public class GridHeaderServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletOperation operations = new ServletOperation();
        SOAPClient client = operations.getClient(request.getSession());
        WSOperation operation = new WSOperation(client);
        GridOperation goperation = new GridOperation(request.getContextPath());
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        boolean visible = false;
        String colModel = "";
        CardUtils cardUtils = new CardUtils();
        CardConfiguration cardConfig = cardUtils.getCardConfiguration(request);
        GridUtils gridUtils = new GridUtils();
        GridConfiguration gridConfig = gridUtils.getGridConfiguration(request);
        List<AttributeSchema> schema = operation.getAttributeList(cardConfig.getClassname());
        if (schema != null) {
            for (AttributeSchema s : schema) {
                setMetadataSessionVariable(s, request.getSession());
                visible = s.isBaseDSP() && !gridConfig.isDisplayOnlyBaseDSP();
            }
        }
        String index = "";

        colModel = "{display:\'ID\', name:\'id\', width:10, fixed: true, hide: true}";
        for (AttributeSchema as : schema) {
            colModel = goperation.generateGridHeaders(as, colModel, index);
        }
        colModel = goperation.generateButtonHeaders(colModel);
        out.write("[" + colModel + "]");
        out.flush();
        out.close();
    }

    private void setMetadataSessionVariable(AttributeSchema as, HttpSession session) {
        List<Metadata> metadata = as.getMetadata();
        if (metadata != null) {
            for (Metadata meta : metadata) {
                if (meta.getKey().equals(CMDBuildTagDictionary.USERID)) {
                    Log.PORTLET.debug("Setting session userid from metadata");
                    session.setAttribute("cmdbmeta.userid", as.getName());
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }
}
