package org.cmdbuild.utils.template;

import java.util.Map;

public class ParameterMapEngine implements TemplateResolverEngine {

	final Map<String, Object> parameterMap;

	public ParameterMapEngine(final Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}

	@Override
	public Object eval(final String expression) {
		return parameterMap.get(expression);
	}

}
