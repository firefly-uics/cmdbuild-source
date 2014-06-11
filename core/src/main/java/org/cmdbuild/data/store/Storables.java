package org.cmdbuild.data.store;

public class Storables {

	private static class StorableById implements Storable {

		private final Long id;

		public StorableById(final Long id) {
			this.id = id;
		}

		@Override
		public String getIdentifier() {
			return id.toString();
		}

	}

	public static Storable storableById(final Long id) {
		return new StorableById(id);
	}

	private Storables() {
		// prevents instantiation
	}

}
