package org.cmdbuild.connector.data;

import org.junit.Assert;
import org.junit.Test;

public class ConnectorClassAttributeTest {

	static final ConnectorClassAttribute Ak_ATTRIBUTE = new ConnectorClassAttribute("a", true);
	static final ConnectorClassAttribute A_ATTRIBUTE = new ConnectorClassAttribute("a");
	static final ConnectorClassAttribute B_ATTRIBUTE = new ConnectorClassAttribute("b");

	@Test
	public void testConnectorClassAttribute() {
		try {
			new ConnectorClassAttribute(null);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new ConnectorClassAttribute(null, false);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new ConnectorClassAttribute(null, true);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new ConnectorClassAttribute("");
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new ConnectorClassAttribute("", false);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new ConnectorClassAttribute("", true);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

	}

	@Test
	public void testGetName() {
		Assert.assertEquals("test", new ConnectorClassAttribute("test").getName());
		Assert.assertEquals("test", new ConnectorClassAttribute("test", true).getName());
		Assert.assertEquals("test", new ConnectorClassAttribute("test", false).getName());
	}

	@Test
	public void testIsKey() {
		Assert.assertEquals(false, new ConnectorClassAttribute("test").isKey());
		Assert.assertEquals(true, new ConnectorClassAttribute("test", true).isKey());
		Assert.assertEquals(false, new ConnectorClassAttribute("test", false).isKey());
	}

	@Test
	public void testEqualsObject() {
		final ConnectorClassAttribute test = new ConnectorClassAttribute("test");
		final ConnectorClassAttribute anotherTest = new ConnectorClassAttribute("test");
		final ConnectorClassAttribute differentTest = new ConnectorClassAttribute("different test");

		Assert.assertEquals(true, test.equals(test));
		Assert.assertEquals(true, test.equals(anotherTest));
		Assert.assertEquals(false, test.equals(differentTest));
	}

	@Test
	public void testCompareTo() {
		final ConnectorClassAttribute a = new ConnectorClassAttribute("a");
		final ConnectorClassAttribute b = new ConnectorClassAttribute("b");
		final ConnectorClassAttribute c = new ConnectorClassAttribute("c");

		Assert.assertTrue(a.compareTo(a) == 0);
		Assert.assertTrue(a.compareTo(b) < 0);
		Assert.assertTrue(a.compareTo(c) < 0);
		Assert.assertTrue(b.compareTo(a) > 0);
		Assert.assertTrue(b.compareTo(b) == 0);
		Assert.assertTrue(b.compareTo(c) < 0);
		Assert.assertTrue(c.compareTo(a) > 0);
		Assert.assertTrue(c.compareTo(b) > 0);
		Assert.assertTrue(c.compareTo(c) == 0);
	}

}
