package org.cmdbuild.connector.differ;

import org.cmdbuild.connector.differ.AbstractDiffer.DifferAction;
import org.junit.Assert;
import org.junit.Test;

public class AbstractDifferTest {

	@Test(expected=IllegalArgumentException.class)
	public void testConstructor_NullCustomerItem() {
		new DifferImpl(null, "test");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testConstructor_NullCMDBuildItem() {
		new DifferImpl("test", null);
	}

	@Test
	public void testListeners() {
		final DifferImpl differImpl = new DifferImpl("test", "test");
		differImpl.addListener(null);
		Assert.assertEquals(0, differImpl.listeners.size());
		differImpl.addListener(new DifferAdapter<String>());
		Assert.assertEquals(1, differImpl.listeners.size());
		differImpl.addListener(new DifferAdapter<String>());
		final DifferAdapter<String> differAdapter = new DifferAdapter<String>();
		differImpl.addListener(differAdapter);
		Assert.assertEquals(3, differImpl.listeners.size());
		differImpl.removeListener(differAdapter);
		Assert.assertEquals(2, differImpl.listeners.size());
		differImpl.removeListener(differAdapter);
		Assert.assertEquals(2, differImpl.listeners.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFireAddItem() {
		new DifferImpl("test", "test").fireAddItem(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFireRemoveItem() {
		new DifferImpl("test", "test").fireRemoveItem(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFireModifyItem() {
		new DifferImpl("test", "test").fireModifyItem(null);
	}

	@Test
	public void testFireItemAction() {
		try {
			new DifferImpl("test", "test").fireItemAction(null, new DifferEvent<String>(this, "test"));
		} catch (final IllegalArgumentException e) {
			// ok
		}

		try {
			new DifferImpl("test", "test").fireItemAction(DifferAction.MODIFY, null);
		} catch (final IllegalArgumentException e) {
			// ok
		}
	}

	private class DifferImpl extends AbstractDiffer<String, String> {

		public DifferImpl(final String customerItem, final String cmdbuildItem) {
			super(customerItem, cmdbuildItem);
		}

		@Override
		public void diff() throws DifferException {
			throw new UnsupportedOperationException();
		}

	}

}
