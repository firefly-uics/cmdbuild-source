package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMDomain;


public interface CMRelation extends CMEntry {

	public interface CMRelationDefinition extends CMEntryDefinition {
		public CMRelationDefinition set(final String key, final Object value);
		public CMRelation save();

		// FIXME
		public CMRelationDefinition setCard1(final CMCard card);
		public CMRelationDefinition setCard2(final CMCard card);
	}

	public CMDomain getType();
}
