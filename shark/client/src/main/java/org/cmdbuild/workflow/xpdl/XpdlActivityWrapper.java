package org.cmdbuild.workflow.xpdl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.workflow.ActivityPerformer;
import org.cmdbuild.workflow.CMActivity;

public class XpdlActivityWrapper implements CMActivity {

	private final XpdlActivity inner;

	XpdlActivityWrapper(final XpdlActivity xpdlActivity) {
		this.inner = xpdlActivity;
	}

	@Override
	public List<ActivityPerformer> getPerformers() {
		List<ActivityPerformer> out = new ArrayList<ActivityPerformer>();
		out.add(getFirstRolePerformer());
		if (inner.isAdminStart()) {
			out.add(ActivityPerformer.newAdminPerformer());
		}
		return out;
	}

	@Override
	public String getName() {
		return inner.getId();
	}

	@Override
	public ActivityPerformer getFirstRolePerformer() {
		return ActivityPerformer.newRolePerformer(inner.getFirstPerformer());
	}

}
