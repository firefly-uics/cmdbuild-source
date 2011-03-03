package org.cmdbuild.connector;

import org.junit.Test;

public class ConnectorTest {

	@Test(expected = IllegalArgumentException.class)
	public void testSetConfiguration() {
		new Connector().setConfiguration(null);
	}

}
