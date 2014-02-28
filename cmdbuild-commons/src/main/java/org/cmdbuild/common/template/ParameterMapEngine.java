package org.cmdbuild.common.template;

import java.util.Map;

public class ParameterMapEngine implements TemplateResolverEngine {

	public static ParameterMapEngine of(final Map<String, Object> map) {
		return new ParameterMapEngine(map);
	}

	private final Map<String, Object> map;

	private ParameterMapEngine(final Map<String, Object> map) {
		this.map = map;
	}

	@Override
	public Object eval(final String expression) {
		return map.get(expression);
	}

}
