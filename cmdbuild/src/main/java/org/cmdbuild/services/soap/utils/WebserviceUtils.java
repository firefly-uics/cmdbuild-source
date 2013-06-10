package org.cmdbuild.services.soap.utils;

import java.util.List;

import javax.xml.ws.handler.MessageContext;

import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

public class WebserviceUtils {

	public String getAuthData(final MessageContext msgCtx) {
		String authData = null;
		if (msgCtx != null) {
			final List<?> v = (List<?>) msgCtx.get(WSHandlerConstants.RECV_RESULTS);
			final WSHandlerResult results = (WSHandlerResult) v.get(0);
			final List<?> wsResults = results.getResults();
			final WSSecurityEngineResult ws = (WSSecurityEngineResult) wsResults.get(0);
			authData = ws.getPrincipal().getName();
		}
		return authData;
	}

}
