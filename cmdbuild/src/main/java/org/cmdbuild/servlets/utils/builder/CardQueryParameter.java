package org.cmdbuild.servlets.utils.builder;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.services.FilterService;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.CQLFacadeCompiler;

public class CardQueryParameter extends AbstractParameterBuilder<CardQuery> {

	public static final String NO_FILTER_PARAMETER = "NoFilter";
	public static final String FILTER_CATEGORY_PARAMETER = "FilterCategory";
	public static final String FILTER_SUBCATEGORY_PARAMETER = "FilterSubcategory";
	public static final String FILTER_FORCE_RESET_PARAMETER = "ForceResetFilter";
	public static final String FILTER_CLASSID = "IdClass";
	public static final String FILTER_CQLQUERY = "CQL";

	public CardQuery build(HttpServletRequest request) {
		String cqlQuery = parameter(String.class, FILTER_CQLQUERY, request);
		if (cqlQuery != null && !(cqlQuery.trim().length() == 0)) {
			return cqlBuild(cqlQuery, request);
		} else {
			return filterServiceBuild(request);
		}
	}

	private CardQuery cqlBuild(String cqlQuery, HttpServletRequest request) {
		Map<String, Object> cqlParams = getCqlParamteres(request);
		return CQLFacadeCompiler.naiveCmbuildCompileSystemUser(cqlQuery, cqlParams);
	}

	private static Map<String, Object> getCqlParamteres(HttpServletRequest req) {
		Map<String, Object> out = new HashMap<String, Object>();
		for (Object oKey : req.getParameterMap().keySet()) {
			String k = (String) oKey;
			String v = req.getParameter(k);
			out.put(k, v);
		}
		return out;
	}

	private CardQuery filterServiceBuild(HttpServletRequest request) {
		CardQuery filter;
		int classId = parameter(Integer.class, FILTER_CLASSID, request);
		if (request.getParameterMap().containsKey(NO_FILTER_PARAMETER)) {
			UserContext userCtx = new SessionVars().getCurrentUserContext();
			filter = userCtx.tables().get(classId).cards().list();
		} else {
			String filterCategory = parameter(String.class, FILTER_CATEGORY_PARAMETER, request);
			String filterSubcategory = parameter(String.class, FILTER_SUBCATEGORY_PARAMETER, request);
			handleForceFilterReset(request, filterCategory, filterSubcategory);
			filter = FilterService.getFilter(classId, filterCategory, filterSubcategory);
		}
		return filter;//.clone();
	}

	private void handleForceFilterReset(HttpServletRequest request, 
			String filterCategory, String filterSubcategory) { 
			boolean resetFilter = parameter(Boolean.class, FILTER_FORCE_RESET_PARAMETER, request); 
			if (resetFilter) { 
				FilterService.clearFilters(filterCategory, filterSubcategory); 
			} 
	} 
}
