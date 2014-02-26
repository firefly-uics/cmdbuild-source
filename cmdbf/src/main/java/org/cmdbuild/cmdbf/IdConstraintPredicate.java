package org.cmdbuild.cmdbf;

import java.util.Set;

import com.google.common.base.Predicate;

public class IdConstraintPredicate implements Predicate<CMDBfId> {
	private Set<CMDBfId> idSet;
	
	public IdConstraintPredicate(Set<CMDBfId> idSet) {
		this.idSet = idSet;
	}

	@Override
	public boolean apply(CMDBfId input) {
		return idSet.contains(input);
	}

}
