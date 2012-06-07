package org.cmdbuild.workflow.service;

import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMParticipant;

public class WMActivityInstanceWrapper implements WSActivityInstInfo {

	final WMActivityInstance inner;

	WMActivityInstanceWrapper(final WMActivityInstance activityInstance) {
		this.inner = activityInstance;
	}

	@Override
	public String getProcessInstanceId() {
		return inner.getProcessInstanceId();
	}

	@Override
	public String getActivityDefinitionId() {
		return inner.getActivityDefinitionId();
	}

	@Override
	public String getActivityInstanceId() {
		return inner.getId();
	}

	@Override
	public String getActivityName() {
		return inner.getName();
	}

	@Override
	public String getActivityDescription() {
		return inner.getDescription();
	}

	@Override
	public String[] getParticipants() {
		final WMParticipant[] participants = inner.getParticipants();
		final String[] names;
		if (participants == null) {
			return new String[0];
		} else {
			names = new String[participants.length];
			for (int i = 0; i < participants.length; ++i) {
				names[i] = participants[i].getName();
			}
		}
		return names;
	}

}
