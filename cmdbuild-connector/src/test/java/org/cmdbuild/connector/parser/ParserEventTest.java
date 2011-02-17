package org.cmdbuild.connector.parser;

import org.junit.Assert;
import org.junit.Test;

public class ParserEventTest {

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_Source() {
		new ParserEvent<String>(null, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_Value() {
		new ParserEvent<String>(new ParserImpl(), null);
	}

	@Test
	public void testGetValue() {
		final String test1 = "test1";
		final String test2 = "test2";
		final ParserEvent<String> event = new ParserEvent<String>(new ParserImpl(), test1);
		Assert.assertEquals(test1, event.getValue());
		Assert.assertNotSame(test2, event.getValue());
	}

}
