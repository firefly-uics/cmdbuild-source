package org.cmdbuild.servlets.utils.builder;

import static org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper.lookupForFlowStatus;
import static org.cmdbuild.dao.legacywrappers.ProcessInstanceWrapper.lookupForFlowStatusCode;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.services.FilterService;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.utils.CQLFacadeCompiler;
import org.cmdbuild.workflow.service.WSProcessInstanceState;

public class CardQueryParameter extends AbstractParameterBuilder<CardQuery> {

	public static final String NO_FILTER_PARAMETER = "NoFilter";
	public static final String FILTER_CATEGORY_PARAMETER = "FilterCategory";
	public static final String FILTER_SUBCATEGORY_PARAMETER = "FilterSubcategory";
	public static final String FILTER_FORCE_RESET_PARAMETER = "ForceResetFilter";
	public static final String FILTER_FLOW_STATUS_PARAMETER = "state";

	public static final String FILTER_CLASSID = "IdClass";
	public static final String FILTER_CQLQUERY = "CQL";

	@Override
	public CardQuery build(final HttpServletRequest request) {
		final String cqlQuery = parameter(String.class, FILTER_CQLQUERY, request);
		if (cqlQuery != null && !(cqlQuery.trim().length() == 0)) {
			return cqlBuild(cqlQuery, request);
		} else {
			return filterServiceBuild(request);
		}
	}

	private CardQuery cqlBuild(final String cqlQuery, final HttpServletRequest request) {
		final Map<String, Object> cqlParams = getCqlParamteres(request);
		return CQLFacadeCompiler.naiveCmbuildCompileSystemUser(cqlQuery, cqlParams);
	}

	private static Map<String, Object> getCqlParamteres(final HttpServletRequest req) {
		final Map<String, Object> out = new HashMap<String, Object>();
		for (final Object oKey : req.getParameterMap().keySet()) {
			final String k = (String) oKey;
			final String v = req.getParameter(k);
			out.put(k, v);
		}
		return out;
	}

	private CardQuery filterServiceBuild(final HttpServletRequest request) {
		CardQuery filter;
		final int classId = parameter(Integer.class, FILTER_CLASSID, request);
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		if (request.getParameterMap().containsKey(NO_FILTER_PARAMETER)) {
			filter = UserOperations.from(userCtx).tables().get(classId).cards().list();
		} else {
			final String filterCategory = parameter(String.class, FILTER_CATEGORY_PARAMETER, request);
			final String filterSubcategory = parameter(String.class, FILTER_SUBCATEGORY_PARAMETER, request);
			handleForceFilterReset(request, filterCategory, filterSubcategory);
			filter = FilterService.getFilter(classId, filterCategory, filterSubcategory);
		}

		if (filter.getTable().isActivity()) {
			setWorkflowParameters(filter, request, userCtx);
		}

		return filter;// .clone();
	}

	private void handleForceFilterReset(final HttpServletRequest request, final String filterCategory,
			final String filterSubcategory) {
		final boolean resetFilter = parameter(Boolean.class, FILTER_FORCE_RESET_PARAMETER, request);
		if (resetFilter) {
			FilterService.clearFilters(filterCategory, filterSubcategory);
		}
	}

	private void setWorkflowParameters(final CardQuery filter, final HttpServletRequest request,
			final UserContext userCtx) {
		setFlowStatus(filter, request);
		filter.setPrevExecutorsFilter(userCtx);
	}

	@Legacy("gonna puke... we pass the SHARK string instead of our own!")
	private void setFlowStatus(final CardQuery filter, final HttpServletRequest request) {
		final String flowStatusCode = request.getParameter(FILTER_FLOW_STATUS_PARAMETER);
		final Lookup flowStatusLookup = lookupForFlowStatusCode(flowStatusCode);
		if (flowStatusLookup != null) {
			filter.filterUpdate(ProcessAttributes.FlowStatus.toString(), AttributeFilterType.EQUALS,
					flowStatusLookup.getId());
		} else {
			// this is "all"... we need to select all the cards, and "clear" the
			// filtering by FlowStatus
			filter.filterUpdate(ProcessAttributes.FlowStatus.toString(), AttributeFilterType.DIFFERENT,
					String.valueOf(lookupForFlowStatus(WSProcessInstanceState.TERMINATED).getId()),
					String.valueOf(lookupForFlowStatus(WSProcessInstanceState.ABORTED).getId()));
		}
	}

}
