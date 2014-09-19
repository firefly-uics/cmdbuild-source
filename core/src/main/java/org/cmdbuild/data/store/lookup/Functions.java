package org.cmdbuild.data.store.lookup;

import com.google.common.base.Function;

public class Functions {

	public static Function<Lookup, LookupType> toLookUpType() {
		return new Function<Lookup, LookupType>() {

			@Override
			public LookupType apply(final Lookup input) {
				return input.type;
			}

		};
	}

	private Functions() {
		// prevents instantiation
	}

}
