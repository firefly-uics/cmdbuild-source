package org.cmdbuild.portlet.operation;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.List;
import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.servlet.ReportServlet;
import org.cmdbuild.services.soap.*;

public class ReportOperation extends WSOperation {

    public static final String ID = "id";
    public static final String EXTENSION = "extension";
    public static final String REPORT_NAME = "reportname";
    public static final String ACTION = "action";
    public static final String EMAIL = "useremail";

    public ReportOperation(SOAPClient client) {
        super(client);
    }

    public void printReport(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<ReportParams> params = ReportServlet.REPORT_PARAMS;
        int id = 0;
        String extension = "";
        String name = "";
        if (params != null) {
            for (ReportParams param : params) {
                if (ID.equals(param.getKey()) && param.getValue() != null) {
                    id = Integer.valueOf(param.getValue());
                }
                if (EXTENSION.equals(param.getKey()) && param.getValue() != null) {
                    extension = param.getValue();
                }
                if (REPORT_NAME.equals(param.getKey()) && param.getValue() != null) {
                    name = param.getValue();
                }
            }
        } else {
            id = Integer.valueOf(request.getParameter(ID));
            extension = request.getParameter(EXTENSION);
            name = request.getParameter(REPORT_NAME);
        }
        String filename = name + "." + extension;

        DataHandler data = getReport(id, extension, params);
        //Clear report parameters list
        ReportServlet.REPORT_PARAMS.clear();
        response.setContentType(data.getContentType());
        response.setHeader("Content-Disposition", String.format("inline; filename=\"%s\";", filename));
        response.setHeader("Expires", "0");
        data.writeTo(response.getOutputStream());
    }

    public void serializeReportParams(HttpServletRequest request) {
        Enumeration parameters = request.getParameterNames();
        while (parameters.hasMoreElements()) {
            String name = (String) parameters.nextElement();
            String value = request.getParameter(name);
            ReportParams params = new ReportParams();
            params.setKey(name);
            params.setValue(value);
            ReportServlet.REPORT_PARAMS.add(params);
        }
    }

    public DataHandler getReport(int id, String extension, List<ReportParams> params) {
        return getService().getReport(id, extension, params);
    }

    public List<AttributeSchema> getReportParameters(int id, String extension) {
        return getService().getReportParameters(id, extension);
    }
}
