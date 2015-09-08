package org.cmdbuild.privileges.predicates;

import org.cmdbuild.dao.entry.CMCard;

import com.google.common.base.Predicate;

public class IsReadableCustomPage implements Predicate<CMCard> {

	@Override
	public boolean apply(final CMCard menuCard) {
		// TODO
		return true;
	}

}
