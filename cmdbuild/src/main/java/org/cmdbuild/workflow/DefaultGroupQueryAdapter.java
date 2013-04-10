package org.cmdbuild.workflow;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.workflow.xpdl.XpdlManager.GroupQueryAdapter;

public class DefaultGroupQueryAdapter implements GroupQueryAdapter {

	@Override
	public String[] getAllGroupNames() {
		final List<String> names = new ArrayList<String>();
		for (final GroupCard groupCard : GroupCard.all()) {
			names.add(groupCard.getName());
		}
		return names.toArray(new String[names.size()]);
	}

}
