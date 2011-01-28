package org.cmdbuild.connector.data;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

public class MutableConnectorClassTest {

	private static final String NAME = "test";

	@SuppressWarnings("serial")
	private static final SortedSet<ConnectorClassAttribute> EMPTY_ATTRIBUTES = new TreeSet<ConnectorClassAttribute>() {
	};

	@SuppressWarnings("serial")
	private static final SortedSet<ConnectorClassAttribute> AB_ATTRIBUTES = new TreeSet<ConnectorClassAttribute>() {
		{
			add(ConnectorClassAttributeTest.A_ATTRIBUTE);
			add(ConnectorClassAttributeTest.B_ATTRIBUTE);
		}
	};

	@SuppressWarnings("serial")
	private static final SortedSet<ConnectorClassAttribute> AkB_ATTRIBUTES = new TreeSet<ConnectorClassAttribute>() {
		{
			add(ConnectorClassAttributeTest.Ak_ATTRIBUTE);
			add(ConnectorClassAttributeTest.B_ATTRIBUTE);
		}
	};

	@SuppressWarnings("serial")
	private static final SortedSet<ConnectorClassAttribute> Ak_ATTRIBUTES = new TreeSet<ConnectorClassAttribute>() {
		{
			add(ConnectorClassAttributeTest.Ak_ATTRIBUTE);
		}
	};

	@SuppressWarnings("serial")
	private static final SortedSet<ConnectorClassAttribute> BAk_ATTRIBUTES = new TreeSet<ConnectorClassAttribute>() {
		{
			add(ConnectorClassAttributeTest.B_ATTRIBUTE);
			add(ConnectorClassAttributeTest.Ak_ATTRIBUTE);
		}
	};

	@Test
	public void testMutableConnector() {
		try {
			new MutableConnectorClass(null, AkB_ATTRIBUTES);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new MutableConnectorClass("", AkB_ATTRIBUTES);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new MutableConnectorClass(NAME, null);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new MutableConnectorClass(NAME, EMPTY_ATTRIBUTES);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new MutableConnectorClass(NAME, AB_ATTRIBUTES);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		new MutableConnectorClass(NAME, AkB_ATTRIBUTES);
	}

	@Test
	public void testGetName() {
		Assert.assertEquals(NAME, new MutableConnectorClass(NAME, AkB_ATTRIBUTES).getName());
	}

	@Test
	public void testGetAttributes() {
		final ConnectorClass connectorClass = new MutableConnectorClass(NAME, AkB_ATTRIBUTES);
		final SortedSet<ConnectorClassAttribute> attributes = connectorClass.getAttributes();
		Assert.assertNotNull(attributes);
		Assert.assertEquals(AkB_ATTRIBUTES, attributes);
	}

	@Test
	public void testGetKeyAttributes() {
		final ConnectorClass connectorClass = new MutableConnectorClass(NAME, AkB_ATTRIBUTES);
		final SortedSet<ConnectorClassAttribute> attributes = connectorClass.getKeyAttributes();
		Assert.assertNotNull(attributes);
		Assert.assertEquals(Ak_ATTRIBUTES, attributes);
	}

	@Test
	public void getCqlQueryString() {
		Assert.assertEquals(null, new MutableConnectorClass(NAME, AkB_ATTRIBUTES, null).getCqlQueryString());
		Assert.assertEquals("", new MutableConnectorClass(NAME, AkB_ATTRIBUTES, "").getCqlQueryString());
		Assert.assertEquals("test", new MutableConnectorClass(NAME, AkB_ATTRIBUTES, "test").getCqlQueryString());
	}

	@Test
	public void testHasAttribute() {
		Assert.assertEquals(false, new MutableConnectorClass(NAME, AkB_ATTRIBUTES).hasAttribute(null));
		Assert.assertEquals(false, new MutableConnectorClass(NAME, AkB_ATTRIBUTES).hasAttribute(""));
		Assert.assertEquals(true, new MutableConnectorClass(NAME, AkB_ATTRIBUTES)
				.hasAttribute(ConnectorClassAttributeTest.A_ATTRIBUTE.getName()));
		Assert.assertEquals(true, new MutableConnectorClass(NAME, AkB_ATTRIBUTES)
				.hasAttribute(ConnectorClassAttributeTest.B_ATTRIBUTE.getName()));
		Assert.assertEquals(false, new MutableConnectorClass(NAME, AkB_ATTRIBUTES).hasAttribute("C"));
	}

	@Test
	public void testGetAttribute() {
		Assert.assertEquals(null, new MutableConnectorClass(NAME, AkB_ATTRIBUTES).getAttribute(null));
		Assert.assertEquals(null, new MutableConnectorClass(NAME, AkB_ATTRIBUTES).getAttribute(""));
		Assert.assertEquals(ConnectorClassAttributeTest.A_ATTRIBUTE, new MutableConnectorClass(NAME, AkB_ATTRIBUTES)
				.getAttribute(ConnectorClassAttributeTest.A_ATTRIBUTE.getName()));
		Assert.assertEquals(ConnectorClassAttributeTest.B_ATTRIBUTE, new MutableConnectorClass(NAME, AkB_ATTRIBUTES)
				.getAttribute(ConnectorClassAttributeTest.B_ATTRIBUTE.getName()));
		Assert.assertEquals(null, new MutableConnectorClass(NAME, AkB_ATTRIBUTES).getAttribute("C"));
	}

	@Test
	public void testCompareTo() {
		final ConnectorClass connectorClass1 = new MutableConnectorClass(NAME, AkB_ATTRIBUTES);
		final ConnectorClass connectorClass2 = new MutableConnectorClass(NAME, BAk_ATTRIBUTES);
		final ConnectorClass connectorClass3 = new MutableConnectorClass(NAME + "1", AkB_ATTRIBUTES);

		Assert.assertTrue(connectorClass1.compareTo(connectorClass1) == 0);
		Assert.assertTrue(connectorClass1.compareTo(connectorClass2) == 0);
		Assert.assertTrue(connectorClass2.compareTo(connectorClass1) == 0);
		Assert.assertTrue(connectorClass1.compareTo(connectorClass3) < 0);
		Assert.assertTrue(connectorClass3.compareTo(connectorClass1) > 0);
	}

	@Test
	public void testEqualsObject() {
		final ConnectorClass aConnectorClass = new MutableConnectorClass(NAME, AkB_ATTRIBUTES);
		final ConnectorClass anotherConnectorClass = new MutableConnectorClass(NAME, AkB_ATTRIBUTES);
		final ConnectorClass differentConnectorClass = new MutableConnectorClass(NAME, BAk_ATTRIBUTES);

		Assert.assertEquals(aConnectorClass, aConnectorClass);
		Assert.assertEquals(aConnectorClass, anotherConnectorClass);
		Assert.assertNotSame(aConnectorClass, differentConnectorClass);
	}

}
