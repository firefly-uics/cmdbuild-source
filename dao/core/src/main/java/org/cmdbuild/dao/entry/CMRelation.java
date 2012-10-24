package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMDomain;

/**
 * Immutable relation.
 */
public interface CMRelation extends CMEntry {

	/**
	 * {@link CMRelation} mutator object.
	 */
	interface CMRelationDefinition extends CMEntryDefinition {

		CMRelationDefinition setCard1(final CMCard card);

		CMRelationDefinition setCard2(final CMCard card);

		@Override
		CMRelationDefinition set(String key, Object value);

		@Override
		CMRelation save();

	}

	@Override
	CMDomain getType();

}
