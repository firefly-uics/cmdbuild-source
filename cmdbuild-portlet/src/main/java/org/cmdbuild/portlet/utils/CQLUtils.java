package org.cmdbuild.portlet.utils;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.services.soap.CqlParameter;
import org.cmdbuild.services.soap.CqlQuery;

public class CQLUtils {

	private static final String CQLQUERY = "CQL";

	public CqlQuery getCQLQuery(final HttpServletRequest request) {
		if (request.getParameter(CQLQUERY) != null && !"".equals(request.getParameter(CQLQUERY))) {
			final CqlQuery query = new CqlQuery();
			query.setCqlQuery(request.getParameter(CQLQUERY));
			final Enumeration parameters = request.getParameterNames();
			int counter = 1;
			while (parameters.hasMoreElements()) {
				final Object parameterObject = parameters.nextElement();
				final String parameterName = parameterObject.toString();
				if (parameterName.equals("p" + counter)) {
					final CqlParameter parameter = new CqlParameter();
					parameter.setKey("p" + counter);
					parameter.setValue(request.getParameter(parameterName));
					query.getParameters().add(parameter);
					counter++;
				}
			}
			return query;
		} else {
			return null;
		}
	}
}
