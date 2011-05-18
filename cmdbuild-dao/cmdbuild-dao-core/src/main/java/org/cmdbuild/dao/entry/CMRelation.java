package org.cmdbuild.dao.entry;


public interface CMRelation extends CMEntry {

	public interface CMRelationDefinition extends CMEntryDefinition {
		public CMRelationDefinition set(final String key, final Object value);
		public CMRelation save();
	}
}
