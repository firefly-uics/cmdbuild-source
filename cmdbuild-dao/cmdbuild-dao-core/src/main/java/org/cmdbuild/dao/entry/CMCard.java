package org.cmdbuild.dao.entry;



public interface CMCard extends CMEntry {
	
	public interface CMCardDefinition extends CMEntryDefinition {
		public CMCardDefinition set(final String key, final Object value);
		public CMCard save();
	}
}
