package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMClass;

public interface CMCard extends CMEntry {
	
	public interface CMCardDefinition extends CMEntryDefinition {
		public CMCardDefinition set(final String key, final Object value);
		public CMCard save();
	}

	public CMClass getType();
}
