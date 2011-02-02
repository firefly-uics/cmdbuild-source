package org.cmdbuild.portlet.operation;

import com.oreilly.servlet.MultipartRequest;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.PortletConfiguration;


public class MultipartedRequestParams extends RequestParams{

    private MultipartRequest multi;

    MultipartedRequestParams(HttpServletRequest request) {
        try {
            int maxUploadSize = PortletConfiguration.getInstance().getMaxUploadSize();
            String tempfile = File.createTempFile("tempdatasource", "").getParent();
            multi = new MultipartRequest(request, tempfile, maxUploadSize);
        } catch (IOException ex) {
            Log.PORTLET.error("Error creating multiparet object", ex);
        }

    }

    @Override
    public Enumeration getParameterNames() {
        return multi.getParameterNames();
    }

    @Override
    public Enumeration getFileNames() {
        return multi.getFileNames();
    }

    @Override
    public String getParameter(String name) {
        return multi.getParameter(name);
    }

    @Override
    public File getFile(String name) {
        return multi.getFile(name);
    }

    @Override
    public String getFilesystemName(String name) {
        return multi.getFilesystemName(name);
    }

}
