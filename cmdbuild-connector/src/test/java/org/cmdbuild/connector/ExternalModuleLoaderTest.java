package org.cmdbuild.connector;

import org.junit.Test;

public class ExternalModuleLoaderTest {

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNullString() {
		new ExternalModuleLoader<ExternalModuleLoaderTest>(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithEmptyString() {
		new ExternalModuleLoader<ExternalModuleLoaderTest>("");
	}

}
