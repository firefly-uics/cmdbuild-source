package org.cmdbuild.connector.parser;

import org.junit.Before;
import org.junit.Test;

public class AbstractParserTest {

	private AbstractParser parser;

	@Before
	public void setUp() throws Exception {
		parser = new ParserImpl();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFireClassFound() {
		parser.fireClassFound(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFireDomainFound() {
		parser.fireDomainFound(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFireCardFound() {
		parser.fireCardFound(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFireRelationFound() {
		parser.fireRelationFound(null);
	}

}
