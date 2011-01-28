package org.cmdbuild.portlet.operation;

import java.io.File;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;


public class SimpleRequestParams extends RequestParams{

    private HttpServletRequest request;

    SimpleRequestParams(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Enumeration getParameterNames() {
        return request.getParameterNames();
    }

    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    @Override
    public Enumeration getFileNames() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public File getFile(String name) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getFilesystemName(String name) {
        throw new UnsupportedOperationException("Not supported.");
    }

}
