package org.cmdbuild.service.rest.cxf.serialization;

public class FakeId {

	// TODO replace with a better solution
	public static Long fakeId(final String s) {
		final Long value;
		if (s != null) {
			final int hashCode = s.hashCode();
			value = (hashCode >= 0) ? Long.valueOf(hashCode) : (long) Integer.MAX_VALUE - hashCode;
		} else {
			value = null;
		}
		return value;
	}

	private FakeId() {
		// prevents instantiation
	}

}
