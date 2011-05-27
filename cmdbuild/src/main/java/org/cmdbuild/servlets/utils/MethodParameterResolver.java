package org.cmdbuild.servlets.utils;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.resource.OutSimpleXML;
import org.cmdbuild.workflow.utils.SimpleXMLDoc;
import org.json.JSONObject;

public class MethodParameterResolver {

	// TODO: polish code..it's pretty messy
	private enum ParameterAnnotation {
		SESSION(Session.class){
			@SuppressWarnings("unchecked")
			@Override
			Object getObj(Annotation arg0, Class arg1, HttpServletRequest arg2) {
				return arg2.getSession().getAttribute(((Session)arg0).value());
			}
			@Override
			boolean isRequired(Annotation arg0) {
				return ((Session)arg0).required();
			}
		},
		REQUEST(Request.class){
			@SuppressWarnings("unchecked")
			@Override
			Object getObj(Annotation arg0, Class arg1, HttpServletRequest arg2) {
				return arg2.getAttribute(((Request)arg0).value());
			}
			@Override
			boolean isRequired(Annotation arg0) {
				return ((Request)arg0).required();
			}
		},
		PARAMETER(Parameter.class){
			@SuppressWarnings("unchecked")
			@Override
			Object getObj(Annotation annot, Class type, HttpServletRequest request) {
				String key = ((Parameter)annot).value();
				
				if( type.isArray() ) {
					Class realType = type.getComponentType();
					String[] values = getParamValues(request,key);
					if(values == null){ 
						Log.JSONRPC.debug(key + " array was not found!");
						return null;
					}
					return ParameterTransformer.getInstance().safeArrayGeneralTransform(realType, request, key, values);
				} else {
					return ParameterTransformer.getInstance().safeTransform(type, request,
							key, getParamValue(request,key));
				}
			}
			@Override
			boolean isRequired(Annotation arg0) {
				return ((Parameter)arg0).required();
			}
		},
		URIPARAM(URIParameter.class){
			@SuppressWarnings("unchecked")
			@Override
			Object getObj(Annotation annot, Class type, HttpServletRequest request) {
				int index = ((URIParameter)annot).value();
				String[] paramValues = getStrippedPath(request);
				String out = null;
				if(index < paramValues.length) {
					out = paramValues[index];
				} else {
					out = "";
				}
				return out;
			}
			@Override
			boolean isRequired(Annotation arg0) {
				return false;
			}
		},
		OUTXML(OutSimpleXML.class){
			@SuppressWarnings("unchecked")
			@Override
			Object getObj(Annotation annot, Class arg1, HttpServletRequest arg2) {
				String rootTagName = ((OutSimpleXML)annot).value();
				return new SimpleXMLDoc(rootTagName);
			}
			@Override
			boolean isRequired(Annotation arg0) {
				return false;
			}
		};
		Class<? extends Annotation> annot;
		private ParameterAnnotation(Class<? extends Annotation> annot) {
			this.annot = annot;
		}
		@SuppressWarnings("unchecked")
		abstract Object getObj( Annotation annot,Class type,HttpServletRequest req );
		abstract boolean isRequired(Annotation annot);
		
		static boolean hasAnnotation( Annotation[] annots ) {
			for(Annotation a : annots) {
				for(ParameterAnnotation pa : values()) {
					if(pa.annot.equals(a.annotationType())) return true;
				}
			}
			return false;
		}
		@SuppressWarnings("unchecked")
		static Object resolve( Annotation[] annots, Class type, HttpServletRequest req )
		throws Exception{
			if(!hasAnnotation(annots)){ return null; }
			for(Annotation a : annots) {
				for(ParameterAnnotation pa : values()) {
					if(pa.annot.equals(a.annotationType())) {
						Object out = pa.getObj(a, type, req);
						if(out == null && pa.isRequired(a)) {
							throw new IllegalArgumentException("Required parameter \""+((Parameter)a).value()+"\" not found!");
						} else {
							return out;
						}
					}
				}
			}
			return null;
		}
	}

	public static final String MultipartRequest = "_multipartParsedParams";
	public static final String RequestPathElements = "_restPathElements";

	
	private static MethodParameterResolver instance = null;

	public static MethodParameterResolver getInstance() {
		if (instance == null) {
			instance = new MethodParameterResolver();
		}
		return instance;
	}

	ParameterBuilder<JSONObject> bcpj = new ParameterBuilder<JSONObject>() {
		public JSONObject build(HttpServletRequest r,OverrideKeys ignored) {
			return new JSONObject();
		};
		public Class<JSONObject> getBindedClass() {
			return JSONObject.class;
		}
	};
	@SuppressWarnings("unchecked")
	ParameterBuilder<Map> bcpm = new ParameterBuilder<Map>() {
		public Map build(HttpServletRequest r,OverrideKeys ignored) {
			return buildParametersMap(r);
		}
		public Class<Map> getBindedClass() {
			return Map.class;
		}
	};

	@SuppressWarnings("unchecked")
	Map<Class, ParameterBuilder> builders;

	@SuppressWarnings("unchecked")
	private MethodParameterResolver() {
		builders = new HashMap();
		builders.put(JSONObject.class, this.bcpj);
		builders.put(Map.class, this.bcpm);
	}

	public <T> void putAutoloadParameter(ParameterBuilder<T> prm) {
		builders.put(prm.getBindedClass(), prm);
	}
	
	public static final String parseKey( String key, HttpServletRequest req ) {
		if( key.indexOf('{') < 0 ){ return key; }
		Pattern varPattern = Pattern.compile("\\{([A-Za-z0-9_]*)\\}");
		Matcher varMatcher = varPattern.matcher(key);
		while( varMatcher.find() ) {
			String varName = varMatcher.group(1);
			String value = getParamValue(req,varName);
			key = varMatcher.replaceFirst(value);
			varMatcher.reset(key);
		}
		return key;
	}

	/**
	 * Resolve method parameters.
	 * Flow:
	 *   1) HttpServlet{Request,Response}
	 *   2) binded class
	 *   3) -- custom if has constructor (User), (Role), (User,Role) or (Role,User)
	 *   4) annotation Session,Request,Parameter,URIParameter,OutSimpleXML
	 * @param types
	 * @param paramsAnnots
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Object[] resolve(Class[] types, Annotation[][] paramsAnnots,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Object[] out = new Object[types.length];

		setupMultipart(request);
		
		for (int i = 0; i < types.length; i++) {
			Class cls = types[i];
			Annotation[] annots = paramsAnnots[i];

			if (annots.length == 0 || !ParameterAnnotation.hasAnnotation(annots)) {
				if(types[i].equals(HttpServletRequest.class)) {
					out[i] = request;
				} else if(types[i].equals(HttpServletResponse.class)) {
					out[i] = response;
				} else {
					OverrideKeys overrides = null;
					if(annots.length > 0) {
						for(Annotation ann : annots) {
							if(ann.annotationType().equals(OverrideKeys.class)) {
								overrides = (OverrideKeys)ann;
								break;
							}
						}
					}
					out[i] = getObjectIfBinded(types[i], request, overrides);
					if (out[i] == null) {
						out[i] = resolveIfUserContextCtor(cls, request);
					}
				}
			} else {
				out[i] = ParameterAnnotation.resolve(annots, cls, request);//resolve(cls, annots, request);
			}

		}

		return out;
	}
	
	@SuppressWarnings("unchecked")
	private void setupMultipart(HttpServletRequest request) throws FileUploadException {
		// parse file items if multipart
		if(isMultipart(request)){
			FileItemFactory factory = new DiskFileItemFactory();

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			List<FileItem> items = upload.parseRequest(request);
			request.setAttribute(MultipartRequest, items);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static String getParamValue(HttpServletRequest request, String key) {
		if (!isMultipart(request)) {
			return request.getParameter(key);
		} else {
			for( FileItem item : (List<FileItem>)request.getAttribute(MultipartRequest) ) {
				if(item.isFormField() && item.getFieldName().equals(key)){
					try {
						return item.getString("utf8");
					} catch (UnsupportedEncodingException e) {
						Log.JSONRPC.error("Wrong encoding for parameter " + key);
						return "";
					}
				}
			}
		}
		return null;
	}
	
	// UNTESTED
	@SuppressWarnings("unchecked")
	public static String[] getParamValues( HttpServletRequest request, String key ) {
		if(!isMultipart(request)) {
			return request.getParameterValues(key);
		} else {
			List<String> values = new ArrayList();
			for( FileItem item : (List<FileItem>)request.getAttribute(MultipartRequest) ) {
				if(item.isFormField() && item.getFieldName().equals(key)){
					values.add(item.getString());
				}
			}
			if(values.size() > 0){
				return values.toArray(new String[]{});
			}
		}
		return null;
	}
	
	public static boolean isMultipart(HttpServletRequest request) {
		return ServletFileUpload.isMultipartContent(request);
	}

	@SuppressWarnings("unchecked")
	private Object getObjectIfBinded(Class type, HttpServletRequest req, OverrideKeys overrides)
			throws Exception {
		ParameterBuilder bcp = builders.get(type);
		if (bcp != null)
			return bcp.build(req,overrides);
		return null;
	}

	private static Map<String, String> buildParametersMap(HttpServletRequest req) {
		Map<String, String> out = new HashMap<String, String>();
		for (Object okey : req.getParameterMap().keySet()) {
			String k = (String) okey;
			String v = req.getParameter(k);
			out.put(k, v);
		}
		return out;
	}
	
	public static String[] getStrippedPath( HttpServletRequest req ) {
		if(null == req.getAttribute(RequestPathElements)) {
			String reqUri = req.getPathInfo();
			String[] out = reqUri.substring(1).split("/");
			req.setAttribute(RequestPathElements, out);
		}
		return (String[])req.getAttribute(RequestPathElements);
	}

	@SuppressWarnings("unchecked")
	private Object resolveIfUserContextCtor(Class type, HttpServletRequest r) {
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		for (Constructor c : type.getConstructors()) {
			Class[] types = c.getParameterTypes();
			if (types.length == 1 && types[0].equals(UserContext.class)) {
				try {
					return c.newInstance(userCtx);
				} catch (Exception e) {
					Log.OTHER.error("Cannot instantiate class " + type.getCanonicalName(), e);
					throw NotFoundExceptionType.PARAMETER_CLASS_UNAVAILABLE.createException(type.getCanonicalName());
				}
			}
		}
		return null;
	}
}
