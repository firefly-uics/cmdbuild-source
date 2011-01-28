package org.cmdbuild.portlet.operation;

import java.io.File;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


public abstract class RequestParams {

    public static RequestParams create (HttpServletRequest request){
        if (ServletFileUpload.isMultipartContent(request)){
            return new MultipartedRequestParams(request);
        } else {
            return new SimpleRequestParams(request);
        }
    }

    public abstract Enumeration getParameterNames();
    public abstract Enumeration getFileNames();
    public abstract String getParameter(String name);
    public abstract File getFile(String name);
    public abstract String getFilesystemName(String name);
}
