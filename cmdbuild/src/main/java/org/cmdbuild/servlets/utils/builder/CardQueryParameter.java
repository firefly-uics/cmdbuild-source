package org.cmdbuild.servlets.utils.builder;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.TableImpl.OrderEntry;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.utils.CQLFacadeCompiler;

public class CardQueryParameter extends AbstractParameterBuilder<CardQuery> {

	public static final String FILTER_CLASSID = "IdClass";
	public static final String FILTER_CLASSNAME = "className";
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
		final ITableFactory tableFactory = UserOperations //
				.from(new SessionVars().getCurrentUserContext()) //
				.tables();
		final ITable table;
		final String className = parameter(String.class, FILTER_CLASSNAME, request);
		if (className != null) {
			table = tableFactory.get(className);
		} else {
			final int classId = parameter(Integer.class, FILTER_CLASSID, request);
			table = tableFactory.get(classId);
		}
		final CardQuery filter = table.cards().list();
		// TODO: Use the default Class filter if present when implemented
		for (final OrderEntry sortEntry : table.getOrdering()) {
			filter.order(sortEntry.getAttributeName(), sortEntry.getOrderDirection());
		}
		return filter;// .clone();
	}

}
