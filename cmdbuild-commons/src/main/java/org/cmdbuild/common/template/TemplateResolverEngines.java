package org.cmdbuild.common.template;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class TemplateResolverEngines {

	private static class EmptyStringOnNullTemplateResolverEngine extends ForwardingTemplateResolverEngine {

		protected EmptyStringOnNullTemplateResolverEngine(TemplateResolverEngine delegate) {
			super(delegate);
		}

		@Override
		public Object eval(final String expression) {
			return defaultIfNull(super.eval(expression), EMPTY);
		}

	}

	private static class NullOnErrorTemplateResolverEngine extends ForwardingTemplateResolverEngine {

		protected NullOnErrorTemplateResolverEngine(TemplateResolverEngine delegate) {
			super(delegate);
		}

		@Override
		public Object eval(final String expression) {
			try {
				return super.eval(expression);
			} catch (final Throwable e) {
				// TODO log
				return null;
			}
		}

	}
	
	public static EmptyStringOnNullTemplateResolverEngine emptyStringOnNull(final TemplateResolverEngine delegate) {
		return new EmptyStringOnNullTemplateResolverEngine(delegate);
	}

	public static NullOnErrorTemplateResolverEngine nullOnError(final TemplateResolverEngine delegate) {
		return new NullOnErrorTemplateResolverEngine(delegate);
	}

	private TemplateResolverEngines() {
		// prevents instantiation
	}

}
