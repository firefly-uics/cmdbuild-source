package org.cmdbuild.utils.template;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateResolverImpl implements TemplateResolver {

	private static final Pattern VAR_PATTERN = Pattern.compile("([^\\{]+)?(\\{(\\w+):(\\w+)\\})?");

	public static class TemplateResolverBuilder {

		private final Map<String, TemplateResolverEngine> engines = new HashMap<String, TemplateResolverEngine>();

		public TemplateResolverBuilder withEngine(final TemplateResolverEngine engine, final String ... prefixes) {
			for (String p : prefixes) {
				engines.put(p, engine);
			}
			return this;
		}

		public TemplateResolverImpl build() {
			return new TemplateResolverImpl(this);
		}
	}

	public static TemplateResolver newInstance(final Map<String, Object> params, final Map<String, Object> dsVars) {
		return new TemplateResolverBuilder()
			.withEngine(new ParameterMapEngine(params), "parm", "form") //
			.withEngine(new ParameterMapEngine(dsVars), "ds", "db") //
			.build();
	}

	public static TemplateResolverBuilder newInstanceBuilder() {
		return new TemplateResolverBuilder();
	}

	private final Map<String, TemplateResolverEngine> engines;

	private TemplateResolverImpl(final TemplateResolverBuilder builder) {
		this.engines = builder.engines;
	}

	public String simpleEval(final String template) {
		final StringBuilder sb = new StringBuilder();
		final Matcher varMatcher = VAR_PATTERN.matcher(template);
		while (varMatcher.find()) {
			final String nonvarPart = varMatcher.group(1);
			final String varPart = varMatcher.group(2);
			if (nonvarPart != null) {
				sb.append(nonvarPart);
			}
			if (varPart != null) {
				final String enginePrefix = varMatcher.group(3);
				final String variable = varMatcher.group(4);
				final Object value = expandVariable(enginePrefix, variable);
				sb.append(String.valueOf(value));
			}
		}
		return sb.toString();
	}

	private Object expandVariable(String enginePrefix, String variable) {
		final TemplateResolverEngine e = engines.get(enginePrefix);
		if (e != null) {
			return e.eval(variable);
		} else {
			return null;
		}
	}
}
