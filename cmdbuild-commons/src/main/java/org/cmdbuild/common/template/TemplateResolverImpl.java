package org.cmdbuild.common.template;

import static java.util.Arrays.asList;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

public class TemplateResolverImpl implements TemplateResolver {

	private static final Pattern VAR_PATTERN = Pattern.compile("([^\\{]+)?(\\{(\\w+):(\\w+)\\})?");

	public static class Builder implements org.apache.commons.lang3.builder.Builder<TemplateResolverImpl> {

		private final Map<String, TemplateResolverEngine> engines = Maps.newHashMap();

		public Builder withEngine(final TemplateResolverEngine engine, final String... prefixes) {
			return withEngine(engine, asList(prefixes));
		}

		public Builder withEngine(final TemplateResolverEngine engine, final Iterable<String> prefixes) {
			for (final String p : prefixes) {
				engines.put(p, engine);
			}
			return this;
		}

		@Override
		public TemplateResolverImpl build() {
			return new TemplateResolverImpl(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Map<String, TemplateResolverEngine> engines;

	private TemplateResolverImpl(final Builder builder) {
		this.engines = builder.engines;
	}

	@Override
	public String simpleEval(final String template) {
		final StringBuilder sb = new StringBuilder();
		final Matcher matcher = VAR_PATTERN.matcher(template);
		while (matcher.find()) {
			final String nonvarPart = matcher.group(1);
			final String varPart = matcher.group(2);
			if (nonvarPart != null) {
				sb.append(nonvarPart);
			}
			if (varPart != null) {
				final String enginePrefix = matcher.group(3);
				final String variable = matcher.group(4);
				final Object value = expandVariable(enginePrefix, variable);
				sb.append(String.valueOf(value));
			}
		}
		return sb.toString();
	}

	private Object expandVariable(final String enginePrefix, final String variable) {
		final TemplateResolverEngine e = engines.get(enginePrefix);
		if (e != null) {
			return e.eval(variable);
		} else {
			return null;
		}
	}

}
