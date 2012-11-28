package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMDomain;


public interface CMRelation extends CMEntry {

	interface CMRelationDefinition extends CMEntryDefinition {
		CMRelationDefinition set(String key, Object value);
		CMRelation save();

		// FIXME
		CMRelationDefinition setCard1(final CMCard card);
		CMRelationDefinition setCard2(final CMCard card);
	}

	CMDomain getType();
}
