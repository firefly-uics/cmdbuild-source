package org.cmdbuild.servlets.resource.shark;

import java.io.File;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.cmdbuild.logger.Log;
import org.cmdbuild.servlets.resource.AbstractResource;
import org.cmdbuild.servlets.resource.RESTExported;
import org.cmdbuild.servlets.resource.RESTExported.RestMethod;
import org.cmdbuild.servlets.utils.URIParameter;

/**
 * This resource expose some files, previously produced by cmdbuild.
 * Once the report is read, it is removed from the filesystem.
 */
@SuppressWarnings("restriction")
public class SharkReportReader extends AbstractResource {

	public String baseURI() {
		return "report";
	}

	public void init(ServletContext ctxt, ServletConfig config) {
	}
	
	@RESTExported(
		httpMethod=RestMethod.GET
	)
	public DataHandler readReport(
		@URIParameter(1) String fileName) {
		String tmpDir = System.getProperty("java.io.tmpdir");
		if(!tmpDir.endsWith(File.separator)) { tmpDir += File.separator; }
		String fpath = tmpDir + fileName;
		
		Log.WORKFLOW.debug("requested report: " + fpath);
		
		//mark the report as deletable
		File f = new File(fpath);
		f.deleteOnExit();
		
		return new DataHandler(new FileDataSource(f));
	}

}
