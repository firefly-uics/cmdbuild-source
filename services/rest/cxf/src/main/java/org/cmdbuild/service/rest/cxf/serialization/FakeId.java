package org.cmdbuild.service.rest.cxf.serialization;

public class FakeId {

	// TODO replace with a better solution
	public static Long fakeId(final String s) {
		final Long value;
		if (s != null) {
			final int hashCode = s.hashCode();
			value = (hashCode < 0) ? Long.MAX_VALUE + hashCode : Long.valueOf(hashCode);
		} else {
			value = null;
		}
		return value;
	}

	private FakeId() {
		// prevents instantiation
	}

}
