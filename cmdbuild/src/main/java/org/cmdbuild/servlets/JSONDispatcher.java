package org.cmdbuild.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.JSONDispatcherService;
import org.cmdbuild.services.JSONDispatcherService.MethodInfo;
import org.cmdbuild.services.auth.AuthenticationFacade;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.JSONBase.Admin;
import org.cmdbuild.servlets.json.JSONBase.Configuration;
import org.cmdbuild.servlets.json.JSONBase.MultipleException;
import org.cmdbuild.servlets.json.JSONBase.PartialFailureException;
import org.cmdbuild.servlets.json.JSONBase.SkipExtSuccess;
import org.cmdbuild.servlets.json.JSONBase.Transacted;
import org.cmdbuild.servlets.json.JSONBase.Unauthorized;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.utils.MethodParameterResolver;
import org.codehaus.jackson.map.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.io.XMLWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class JSONDispatcher extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws IOException, ServletException {
		dispatch(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws IOException, ServletException {
		dispatch(request, response);
	}

	public void dispatch(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException, ServletException {
		httpResponse.setCharacterEncoding("UTF-8");
		String url = getMethodUrl(httpRequest);
		MethodInfo methodInfo = JSONDispatcherService.getInstance().getMethodInfoFromURL(url);
		if (methodInfo == null) {
			Log.JSONRPC.warn("Method not found for URL " + url);
			httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		try {
			try {
				if (methodInfo.getMethod().getAnnotation(Unauthorized.class) == null) {
					checkAuthentication(httpRequest);
				}
				Admin adminAnnotation = methodInfo.getMethod().getAnnotation(Admin.class);
				if (adminAnnotation != null) {
					checkAdmin(httpRequest, adminAnnotation.value());
				}
				if (methodInfo.getMethod().getAnnotation(Configuration.class) != null)
					checkUnconfigured();
				startTransactionIfNeeded(methodInfo);
				
				JSONBase targetClass = (JSONBase) methodInfo.getMethod().getDeclaringClass().newInstance();
				targetClass.init(httpRequest, httpResponse);
				setSpringApplicationContext(targetClass);

				Class<?>[] types = methodInfo.getParamClasses();
				Annotation[][] paramsAnnots = methodInfo.getParamsAnnotations();

				Object[] params = MethodParameterResolver.getInstance().resolve(types, paramsAnnots, httpRequest, httpResponse);
				Object methodResponse = methodInfo.getMethod().invoke(targetClass, params);

				writeResponse(methodInfo, methodResponse, httpRequest, httpResponse);
				commitTransaction();
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		} catch (Throwable t) {
			rollbackTransaction();
			logError(methodInfo, t);
			writeErrorMessage(methodInfo, t, httpRequest, httpResponse);
		}
	}

	private void setSpringApplicationContext(JSONBase targetClass) {
		final ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		targetClass.setSpringApplicationContext(applicationContext);
	}

	private void logError(MethodInfo methodInfo, Throwable t) {
		if (Log.JSONRPC.isDebugEnabled()) {
			Log.JSONRPC.debug("Uncaught exception calling method " + methodInfo, t);
		} else {
			StringBuffer message = new StringBuffer();
			message.append("A ")
				.append(t.getClass().getCanonicalName())
				.append(" occurred calling method ")
				.append(methodInfo);
			if (t.getMessage() != null) {
				message.append(": ").append(t.getMessage());
			}
			Log.JSONRPC.error(message.toString());
		}
	}

	private void rollbackTransaction() {
		try {
			Connection con = DBService.getConnection();
			if (!con.getAutoCommit())
				con.rollback();
		} catch (ORMException e) {
			Log.OTHER.debug("Rollback never needed if the connection is not configured");
		} catch (SQLException e) {
			Log.OTHER.error("Can't rollback the transaction!", e);
		}
	}

	private void commitTransaction() throws SQLException {
		try {
			Connection con = DBService.getConnection();
			if (!con.getAutoCommit())
				con.commit();
		} catch (ORMException e) {
			Log.OTHER.debug("Commit never needed if the connection is not configured");
		} catch (SQLException e) {
			Log.OTHER.error("Can't commit the transaction!", e);
			throw e;
		}
	}

	private void startTransactionIfNeeded(MethodInfo methodInfo) throws SQLException {
		if (methodInfo.getMethod().getAnnotation(Transacted.class) != null) {
			DBService.getConnection().setAutoCommit(false);
		}
	}

	private void checkUnconfigured() {
		if(DatabaseProperties.getInstance().isConfigured())
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
	}

	/*
	 * This method does not allow redirection
	 */
	private void checkAuthentication(final HttpServletRequest httpRequest) {
		try {
			if (AuthenticationFacade.isLoggedIn(httpRequest)) {
				return;
			}
		} catch (Exception e) {
		}
		throw AuthExceptionType.AUTH_NOT_LOGGED_IN.createException();
	}

	private void checkAdmin(HttpServletRequest httpRequest, AdminAccess adminAccess) {
		AuthenticationFacade.assureAdmin(httpRequest, adminAccess);
	}

	private String getMethodUrl(HttpServletRequest httpRequest) {
		String url = httpRequest.getPathInfo();
		// Legacy method call
		String legacyMethod = httpRequest.getParameter("method");
		if (legacyMethod != null) {
			url += "/"+legacyMethod.toLowerCase();
			Log.JSONRPC.warn("Using legacy method specification for url " + url);
	    } else {
	    	Log.JSONRPC.info("Calling url " + url);
	    }
		if (Log.JSONRPC.isDebugEnabled()) {
			printRequestParameters(httpRequest);
		}
		return url;
	}

	@SuppressWarnings("unchecked")
	private void printRequestParameters(HttpServletRequest httpRequest) {
    	Map<String, String[]> parameterMap = (Map<String, String[]>) httpRequest.getParameterMap();
    	for (String parameterName : parameterMap.keySet()) {
    		if ("method".equals(parameterName))
    			continue;
    		String[] parameterValues = parameterMap.get(parameterName);
    		String printableParameterValue;
    		if (!Log.JSONRPC.isTraceEnabled() && parameterName.toLowerCase().contains("password")) {
    			printableParameterValue = "***";
    		} else {
    			printableParameterValue = parameterValueToString(parameterValues);
    		}
			Log.JSONRPC.debug(String.format("    parameter \"%s\": %s", parameterName, printableParameterValue));
    	}
	}

	private String parameterValueToString(String[] parameterValues) {
		String printValue;
		if (parameterValues.length == 1) {
			printValue = parameterValues[0];
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append("{\"");
			for (int i = 0; i<parameterValues.length; ++i) {
				if (i != 0)
					sb.append("\", \"");
				sb.append(parameterValues[i]);
			}
			sb.append("\"}");
			printValue = sb.toString();
		}
		return printValue;
	}

	private void writeResponse(MethodInfo methodInfo, Object methodResponse, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws JSONException, IOException {
		methodResponse = addSuccessAndWarningsIfJSON(methodInfo, methodResponse);
		setContentType(methodInfo, methodResponse, httpRequest, httpResponse);
		writeResponseData(methodInfo, methodResponse, httpResponse);
	}

	private Object addSuccessAndWarningsIfJSON(MethodInfo javaMethod, Object methodResponse) throws JSONException {
		if (javaMethod.getMethod().getAnnotation(SkipExtSuccess.class) != null) {
			return methodResponse;
		}
		Class<?> returnType = javaMethod.getMethod().getReturnType();
		if (Void.TYPE == returnType) {
			return addSuccessAndWarningsToJSONObject(new JSONObject());
		} else if (methodResponse instanceof JSONObject) {
			JSONObject jres = (JSONObject) methodResponse;
			return addSuccessAndWarningsToJSONObject(jres);
		} else if (methodResponse instanceof JsonResponse) {
			return serializeJsonResponse();
		} else {
			return methodResponse;
		}
	}

	private Object serializeJsonResponse() {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			return "{\"success\":false}";
		}
	}

	private Object addSuccessAndWarningsToJSONObject(JSONObject jres) throws JSONException {
		addRequestWarnings(jres);
		// Login.login() sets success to false instead of throwing the exception
		if (!jres.has("success")) {
			jres.put("success", true);
		}
		return jres;
	}

	private void setContentType(MethodInfo methodInfo, Object methodResponse,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		if (MethodParameterResolver.isMultipart(httpRequest)) {
			/**
			 * Ext fake the ajax call with an iframe, and we need to tell the browser to
			 * not change the text (if not, the json response will be surrounded by a "pre" tag).
			 */
			httpResponse.setContentType("text/html");
		} else if (methodResponse instanceof DataHandler) {
			DataHandler dh = (DataHandler) methodResponse;
			httpResponse.setContentType(dh.getContentType());
		} else if (methodResponse instanceof String) {
			httpResponse.setContentType("text/plain");
		} else {
			httpResponse.setContentType(methodInfo.getMethodAnnotation().contentType());
		}
	}

	private void writeResponseData(MethodInfo methodInfo,
			Object methodResponse, HttpServletResponse httpResponse) throws IOException {
		if (methodResponse instanceof DataHandler) {
			DataHandler dh = (DataHandler) methodResponse;
			httpResponse.setHeader("Content-Disposition", String.format("inline; filename=\"%s\";", dh.getName()));
			httpResponse.setHeader("Expires","0");
			dh.writeTo(httpResponse.getOutputStream());
		} else if (methodResponse instanceof Document) {
			XMLWriter writer = new XMLWriter(httpResponse.getWriter());
			writer.write(methodResponse);
		} else {
			httpResponse.getWriter().write(methodResponse.toString());
		}
	}

	private void writeErrorMessage(MethodInfo methodInfo, Throwable exception, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
		if (methodInfo == null) {
			httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		Class<?> returnType = methodInfo.getMethod().getReturnType();
		if (DataHandler.class == returnType || 
				methodInfo.getMethod().getAnnotation(SkipExtSuccess.class) != null) {
			if (exception instanceof NotFoundException) {
				httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} else if (exception instanceof AuthException) {
				httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else {
				httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			return;
		} else {
			if (MethodParameterResolver.isMultipart(httpRequest)) {
				httpResponse.setContentType("text/html");
			} else {
				httpResponse.setContentType("application/json");
			}
			try {
				final JSONObject jsonOutput = getOutput(exception);
				addErrors(jsonOutput, exception);
				jsonOutput.put("success", false);
				httpResponse.getWriter().write(jsonOutput.toString());
			} catch (JSONException e) {
				Log.OTHER.error("Can't serialize the exception", e);
			}
		}
	}

	public JSONObject getOutput(Throwable exception) {
		final JSONObject jsonOutput;
		if (exception instanceof PartialFailureException) {
			jsonOutput = ((PartialFailureException) exception).getPartialOutput();
		} else {
			jsonOutput = new JSONObject();
		}
		return jsonOutput;
	}

	private void addErrors(JSONObject jsonOutput, Throwable exception) throws JSONException {
		if (exception instanceof PartialFailureException) {
			exception = ((PartialFailureException) exception).getOriginalException();
		}
		if (exception instanceof MultipleException) {
			final MultipleException me = (MultipleException) exception;
			jsonOutput.put("errors", serializeExceptionArray(me.getExceptions()));
		} else {
			jsonOutput.append("errors", serializeException(exception));
		}
	}

	private void addRequestWarnings(JSONObject jsonOutput) throws JSONException {
		List<? extends Throwable> warnings = RequestListener.getCurrentRequest().getWarnings();
		if (!warnings.isEmpty()) {
			jsonOutput.put("warnings", serializeExceptionArray(warnings));
		}
	}

	public JSONArray serializeExceptionArray(Iterable<? extends Throwable> exceptions) throws JSONException {
		JSONArray exceptionArray = new JSONArray();
		for (Throwable t : exceptions) {
			exceptionArray.put(serializeException(t));
		}
		return exceptionArray;
	}

	static private JSONObject serializeException(Throwable e) throws JSONException {
		JSONObject exceptionJson = new JSONObject();
		if (e instanceof CMDBException) {
			CMDBException ce = (CMDBException) e;
			exceptionJson.put("reason", ce.getExceptionTypeText());
			exceptionJson.put("reason_parameters", JSONDispatcher.serializeExceptionParameters(ce.getExceptionParameters()));
		}
		addStackTrace(exceptionJson, e);
		return exceptionJson;
	}

	static private JSONArray serializeExceptionParameters(String[] exceptionParameters) {
		JSONArray jsonParameters = new JSONArray();
		if(exceptionParameters != null) {
			for (int i = 0; i < exceptionParameters.length; ++i) {
				jsonParameters.put(exceptionParameters[i]);
			}
		}
		return jsonParameters;
	}

	private static void addStackTrace(JSONObject exceptionJson, Throwable t) throws JSONException {
		StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		sw.flush();
		exceptionJson.put("stacktrace", sw.toString());
	}
}
