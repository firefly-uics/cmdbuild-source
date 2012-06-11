package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMClass;

/**
 * Immutable card
 */
public interface CMCard extends CMEntry {
	
	interface CMCardDefinition extends CMEntryDefinition {
		CMCardDefinition set(String key, Object value);
		CMCard save();
	}

	CMClass getType();
}
