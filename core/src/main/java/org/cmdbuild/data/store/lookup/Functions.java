package org.cmdbuild.data.store.lookup;

import com.google.common.base.Function;

public class Functions {

	private static final Function<Lookup, Long> LOOKUP_ID = new Function<Lookup, Long>() {

		@Override
		public Long apply(final Lookup input) {
			return input.getId();
		}

	};

	private static final Function<Lookup, LookupType> LOOKUP_TYPE = new Function<Lookup, LookupType>() {

		@Override
		public LookupType apply(final Lookup input) {
			return input.type();
		}

	};

	public static Function<Lookup, Long> toLookupId() {
		return LOOKUP_ID;
	}

	public static Function<Lookup, LookupType> toLookupType() {
		return LOOKUP_TYPE;
	}

	private Functions() {
		// prevents instantiation
	}

}
