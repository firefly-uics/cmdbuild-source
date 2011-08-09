package org.cmdbuild.servlets;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.servlets.resource.BasicProtectedResource;
import org.cmdbuild.servlets.resource.RESTExported;
import org.cmdbuild.servlets.resource.Resource;
import org.cmdbuild.servlets.resource.RESTExported.RestMethod;
import org.cmdbuild.servlets.utils.MethodParameterResolver;

/**
 * Servlet that implements Restful services.
 * Used internally to communicate with Shark, eventually can be used to serve other purposes.
 */
@SuppressWarnings("restriction")
public class RestServlet extends HttpServlet {
	
	/**
	 * Analog to the JSONDispatcherService.MethodInfo, stores also the RestExposed annotation.
	 */
	public class RestMethodInfo {
		RESTExported restExposed;
		Method method;
		@SuppressWarnings("unchecked")
		Class[] parameterTypes;
		Annotation[][] parametersAnnots;
		
		public RestMethodInfo(Method method, RESTExported annot) {
			this.method = method;
			this.restExposed = annot;
			this.parametersAnnots = method.getParameterAnnotations();
			this.parameterTypes = method.getParameterTypes();
		}
		public String toString() {
			return method.getDeclaringClass() + "." + method.getName();
		}
	}

	private static final long serialVersionUID = 1L;
	
	/**
	 * key to set/get the Resource Object in the Request attribute map.
	 */
	static final String RequestResource = "_RestResource";
	
	List<Resource> resources;
	
	Map<Class<? extends Resource>,List<RestMethodInfo>> mapping;

	private Resource resolve( HttpServletRequest request, String[] path ) {
		for(Resource res : resources) {
			if(res.match(path[0]))
				return res;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void init() throws ServletException {
		String packName = this.getServletConfig().getInitParameter("resourcesPackage");
		String resourcesString = this.getServletConfig().getInitParameter("resources");
		mapping = new HashMap();
		resources = new ArrayList();
		for(String resString : resourcesString.split(",")) {
			String clsName = resString;
			if(packName != null) {
				clsName = packName + "." + clsName;
			}
			Log.REST.debug("add resource class: " + clsName);
			try {
				Class<Resource> cls = (Class<Resource>)Class.forName(clsName);
				for(Method m : cls.getMethods()) {
					RESTExported re = (RESTExported)m.getAnnotation(RESTExported.class);
					if(re != null) {
						if(!mapping.containsKey(cls)) {
							mapping.put(cls, new ArrayList());
						}
						mapping.get(cls).add(new RestMethodInfo(m,re));
					}
				}
				Resource res = cls.newInstance();
				res.init(getServletContext(), getServletConfig());
				resources.add(res);
				Log.REST.debug("..resource added");
			} catch (Exception e) {
				Log.OTHER.error("Cannot instantiate REST class " + clsName, e);
				throw NotFoundExceptionType.PARAMETER_CLASS_UNAVAILABLE.createException(resString);
			}
		}
	}
	
	/**
	 * Check for associated resource and handle basic authentication
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String[] pathElements = retrievePathElements(req);
		Resource res = resolve(req, pathElements);
		if(res == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		if(res instanceof BasicProtectedResource) {
			BasicProtectedResource bpr = (BasicProtectedResource)res;
			String auth = req.getHeader("Authorization");
			if(auth == null) {
				resp.setHeader("WWW-Authenticate", "Basic realm=\"" + bpr.authRealm() + "\"");
				resp.sendError(401); //unauthorized
				return;
			}
			
			if(!"Basic".equalsIgnoreCase(auth.substring(0, 5))) {
				resp.sendError(400); //unsupported authentication method, so bad request
				return;
			}
			
			String userCtx = "";
			try {
				userCtx = new String(Base64.decode( auth.replace("Basic ", "") ));
				Log.REST.debug("basic authorization info: " + userCtx);
			} catch (Base64DecodingException e) {
				resp.sendError(400);
				return;
			}
			
			String[] toks = userCtx.split(":");
			Log.REST.debug("check user " + toks[0] + ", password " + toks[1]);
			if( !bpr.checkAuthentication(toks[0], toks[1], req) ) {
				resp.setHeader("WWW-Authenticate", "Basic realm=\"" + bpr.authRealm() + "\"");
				resp.sendError(401); //unauthorized
				return;
			}
			
			Log.REST.debug("basic protected resource: check passed.");
		}
		
		req.setAttribute(RequestResource, res);
		
		super.service(req, resp);
	}
	
	private Resource retrieveResource(HttpServletRequest req) {
		return (Resource)req.getAttribute(RequestResource);
	}
	private String[] retrievePathElements(HttpServletRequest req){
		return MethodParameterResolver.getStrippedPath(req);
	}
	
	private RestMethodInfo resolveMethodInfo( HttpServletRequest req, RestMethod httpMethod ) {
		Resource res = retrieveResource(req);
		String[] path = retrievePathElements(req);
		
		for(RestMethodInfo mi : mapping.get(res.getClass())) {
			if(mi.restExposed.httpMethod().equals(httpMethod)) {
				if( 
					"".equals(mi.restExposed.subResource()) ||
					(path.length >= 2 &&
					path[1].equals(mi.restExposed.subResource()))
				) {
					return mi;
				}
			}
		}
		return null;
	}

	private void execute( HttpServletRequest req, HttpServletResponse resp, RestMethod httpMethod ) throws IOException {
		Log.REST.info(String.format("Rest method requested: %s", req.getRequestURI()));
		RestMethodInfo mInfo = resolveMethodInfo(req, httpMethod);
		if(mInfo == null) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		
		Object[] params = null;
		try {
			params = MethodParameterResolver.getInstance().resolve(mInfo.parameterTypes, mInfo.parametersAnnots, req,resp);
		} catch (Exception e) {
			Log.REST.warn("Cannot resolve parameters");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
		Object response = null;
		try {
			response = mInfo.method.invoke(this.retrieveResource(req), params);
		} catch (Exception e) {
			Log.REST.warn("Exception invoking method " + mInfo, e);
			resp.sendError( mInfo.restExposed.failureCode() );
			return;
		}
		
		resp.setStatus(mInfo.restExposed.successCode());
		if(response != null) {
			resp.setContentType(mInfo.restExposed.contentType());
			resp.setCharacterEncoding("UTF8");
			if( response instanceof DataHandler ) {
				//this is a file
				DataHandler dh = (DataHandler)response;
				resp.setContentType(dh.getContentType());
				resp.setHeader( "Content-Disposition","attachment; filename=" + dh.getName() + ";" );
				dh.writeTo(resp.getOutputStream());
			} else {
				resp.getWriter().print(response.toString());
				resp.getWriter().flush();
			}
		}
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req,resp, RestMethod.DELETE);
	}
	
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req,resp, RestMethod.HEAD);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req,resp, RestMethod.GET);
	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req,resp, RestMethod.OPTIONS);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req,resp, RestMethod.POST);
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req,resp, RestMethod.PUT);
	}
	
	@Override
	protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req,resp, RestMethod.TRACE);
	}

}
