package org.cmdbuild.servlets;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.cmdbuild.cmdbf.xml.XmlRegistry;
import org.cmdbuild.logic.auth.AuthenticationLogicUtils;
import org.cmdbuild.logic.cache.CachingLogic;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public class XsdSchema extends HttpServlet {

	private static final long serialVersionUID = 1L;

	ApplicationContext applicationContext;
	
	public void init() throws ServletException {
		super.init();	
		applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
    }
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			AuthenticationLogicUtils.assureAdmin(request, AdminAccess.FULL);
			XmlRegistry xmlRegistry = applicationContext.getBean(XmlRegistry.class); 
			
			URI baseUri = URI.create(request.getRequestURL().toString());
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			if(ServletFileUpload.isMultipartContent(request)) {
				@SuppressWarnings("unchecked")
				List<FileItem> items = upload.parseRequest(request);
				for (FileItem item : items) {
					if(!item.isFormField()) {
						XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
		    			URI uri = baseUri.resolve(item.getName());
						InputSource source = new InputSource(uri.toString());
						InputStream filecontent = item.getInputStream();
						source.setByteStream(filecontent);
						XmlSchema schema = schemaCollection.read(source, null);
						xmlRegistry.updateSchema(schema);
					}
				}
				cachingLogic().clearCache();
		 	}
			
			response.setContentType("text/html");
			Writer writer = response.getWriter();
			writer.write("<html><head><title>Xml Schema Upload</title></head><body>");
			writer.write("OK");
			writer.write("</body></html>");
			writer.flush();
			
	    } catch (Exception e) {
	    	response.setContentType("text/html");
			Writer writer = response.getWriter();
			writer.write("<html><head><title>Xml Schema Upload</title></head><body>");
			writer.write(e.getMessage());
			writer.write("</body></html>");
			writer.flush();
	        throw new ServletException(e.getMessage(), e);
	    }
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			XmlRegistry xmlRegistry = applicationContext.getBean("xmlRegistry", XmlRegistry.class); 
			String pathInfo = request.getPathInfo();
			if(pathInfo != null && pathInfo.startsWith("/"))
				pathInfo = pathInfo.substring(1);
			if(pathInfo == null || pathInfo.isEmpty()) {
				response.setContentType("text/html");
				Writer writer = response.getWriter();
				
				writer.write("<html>\n" +
	 				         "  <head>\n" +
	 				         "<title>CMDB Xml Schema</title>\n" +
					         "  </head>\n" +
						     "  <body>\n" +
						     "    <p>\n" +
						     "      <form method='POST' accept-charset='UTF-8' enctype='multipart/form-data'>\n" +
							 "        Xml schema: <input type='file' name='xsd'><br>\n" +
				  			 "        <input type='submit' value='Upload'>\n" +
							 "      </form>\n" +
				  			 "    </p>\n" +
						     "    <p>\n" +
		    				 "    <ul>\n");
				for(String systemId : xmlRegistry.getSystemIds())
					writer.write("<li><a href=\"" + systemId + "\">" + systemId + "</a></li>\n");
				writer.write("    </ul>\n" +
						     "    </p>\n" +
							 "  </body>\n" +
							 "</html>");
				writer.flush();
			}
			else {
				XmlSchema schema = xmlRegistry.getSchema(pathInfo);
				if(schema != null) {										
					Document schemaDocument = schema.getSchemaDocument();
					DOMImplementationLS domImplementation = (DOMImplementationLS) schemaDocument.getImplementation();
					LSSerializer lsSerializer = domImplementation.createLSSerializer();
					LSOutput destination = domImplementation.createLSOutput();
					destination.setByteStream(response.getOutputStream());
					destination.setEncoding("UTF-8");
					response.setCharacterEncoding(destination.getEncoding());
					response.setContentType("text/xml");
					lsSerializer.write(schemaDocument, destination);
				}
			}
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
	}
	
	private CachingLogic cachingLogic() {
		return applicationContext().getBean(CachingLogic.class);
	}
}