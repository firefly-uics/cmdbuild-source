package org.cmdbuild.portlet.operation;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.PortletConfiguration;

import com.oreilly.servlet.MultipartRequest;

public class MultipartedRequestParams extends RequestParams {

	private MultipartRequest multi;

	MultipartedRequestParams(final HttpServletRequest request) {
		try {
			final int maxUploadSize = PortletConfiguration.getInstance().getMaxUploadSize();
			final String tempfile = File.createTempFile("tempdatasource", "").getParent();
			multi = new MultipartRequest(request, tempfile, maxUploadSize);
		} catch (final IOException ex) {
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
	public String getParameter(final String name) {
		return multi.getParameter(name);
	}

	@Override
	public File getFile(final String name) {
		return multi.getFile(name);
	}

	@Override
	public String getFilesystemName(final String name) {
		return multi.getFilesystemName(name);
	}

}
