package org.cmdbuild.services.soap.utils;

import java.util.List;

import javax.xml.ws.handler.MessageContext;

import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

public class WebserviceUtils {
	
	public String getAuthData(MessageContext msgCtx) {
		String authData = null;
		if (msgCtx != null) {
			List<?> v = (List<?>) msgCtx.get(WSHandlerConstants.RECV_RESULTS); 
			WSHandlerResult results = (WSHandlerResult) v.get(0);
			List<?> wsResults = results.getResults();
			WSSecurityEngineResult ws = (WSSecurityEngineResult) wsResults.get(0);
			authData = ws.getPrincipal().getName();
		}
		return authData;
	}

}
