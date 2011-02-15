package org.cmdbuild.connector.data;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class KeyTest {

	@Test
	public void testKey() {
		try {
			new Key();
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new Key((KeyValue[]) null);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new Key((List<KeyValue>) null);
			Assert.fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// ok
		}

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateKeyValueStringArrayEmpty() {
		Key.createKeyValue();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateKeyValueStringArrayNull() {
		Key.createKeyValue((String[]) null);
	}

	@Test(expected = NullPointerException.class)
	public void testCreateKeyValueKeyNull() {
		Key.createKeyValue((Key) null);
	}

	@Test
	public void testCompareTo() {
		final Key a = new Key(Key.createKeyValue("a"));
		final Key a2 = new Key(Key.createKeyValue("a"));
		final Key b = new Key(Key.createKeyValue("b"));
		final Key ac = new Key(Key.createKeyValue("a", "c"));
		final Key ad = new Key(Key.createKeyValue("a", "d"));

		Assert.assertTrue(a.compareTo(a2) == 0);
		Assert.assertTrue(a2.compareTo(a) == 0);
		Assert.assertTrue(a.compareTo(b) < 0);
		Assert.assertTrue(b.compareTo(a) > 0);
		Assert.assertTrue(ac.compareTo(ad) < 0);
		Assert.assertTrue(ad.compareTo(ac) > 0);
	}

	@Test
	public void testEqualsObject() {
		final Key a = new Key(Key.createKeyValue("a"));
		final Key a2 = new Key(Key.createKeyValue("a"));
		final Key b = new Key(Key.createKeyValue("b"));
		final Key ac = new Key(Key.createKeyValue("a", "c"));
		final Key ad = new Key(Key.createKeyValue("a", "d"));

		Assert.assertFalse(a.equals(null));
		Assert.assertFalse(a.equals("test"));
		Assert.assertTrue(a.equals(a2));
		Assert.assertTrue(a2.equals(a));
		Assert.assertFalse(a.equals(b));
		Assert.assertFalse(b.equals(a));
		Assert.assertFalse(a.equals(ac));
		Assert.assertFalse(a.equals(ad));
		Assert.assertFalse(ac.equals(ad));
		Assert.assertFalse(ad.equals(ac));
	}

}
