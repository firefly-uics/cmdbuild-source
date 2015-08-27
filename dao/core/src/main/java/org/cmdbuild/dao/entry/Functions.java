package org.cmdbuild.dao.entry;

import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;

import com.google.common.base.Function;

public class Functions {

	private static class ToCode implements Function<CMCard, String> {

		@Override
		public String apply(final CMCard input) {
			return input.get(CODE_ATTRIBUTE, String.class);
		}

	}

	private static final ToCode TO_CODE = new ToCode();

	public static Function<CMCard, String> toCode() {
		return TO_CODE;
	}

	private Functions() {
		// prevents instantiation
	}

}
