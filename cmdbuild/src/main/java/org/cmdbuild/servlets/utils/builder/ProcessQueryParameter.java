package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.ProcessQueryImpl;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ProcessQuery;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;

public class ProcessQueryParameter extends AbstractParameterBuilder<ProcessQuery> {

	public static final String FILTER_CATEGORY_PARAMETER = "FilterCategory";
	public static final String FILTER_SUBCATEGORY_PARAMETER = "FilterSubcategory";
	public static final String FILTER_CLASSID = "IdClass";
	public static final String FILTER_CQLQUERY = "CQL";

	public ProcessQuery build(HttpServletRequest request) {
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		CardQueryParameter cqp = new CardQueryParameter();
		CardQuery cardQuery = cqp.build(request);
		return new ProcessQueryImpl(cardQuery, userCtx);
	}
}
