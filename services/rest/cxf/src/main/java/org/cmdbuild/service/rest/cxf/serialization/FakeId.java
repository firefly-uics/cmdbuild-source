package org.cmdbuild.service.rest.cxf.serialization;

public class FakeId {

	// TODO replace with a better solution
	public static Long fakeId(final String s) {
		final int hashCode = s.hashCode();
		return (hashCode < 0) ? Long.MAX_VALUE + hashCode : Long.valueOf(hashCode);
	}

	private FakeId() {
		// prevents instantiation
	}

}
