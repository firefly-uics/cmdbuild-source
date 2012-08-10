package org.cmdbuild.workflow;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.common.annotations.Legacy;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.client.wfservice.XPDLBrowser;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.api.internal.eventaudit.EventAuditException;
import org.enhydra.shark.api.internal.eventaudit.StateEventAuditPersistenceObject;

public class SharkEventsDelegator extends NullEventAuditManager {

	private static enum EventType {
		PROCESS_STATE_CHANGED(SharkConstants.EVENT_PROCESS_STATE_CHANGED), //
		ACTIVITY_STATE_CHANGED(SharkConstants.EVENT_ACTIVITY_STATE_CHANGED), //
		UNKNOWN(null), //
		;

		private final String sharkEventType;

		private EventType(final String sharkEventType) {
			this.sharkEventType = sharkEventType;
		}

		public static EventType fromSharkEventType(final String eventType) {
			if (eventType != null) {
				for (final EventType et : EventType.values()) {
					if (eventType.equals(et.sharkEventType)) {
						return et;
					}
				}
			}
			return UNKNOWN;
		}

	}

	private static enum RunningStates {
		OPEN_RUNNING(SharkConstants.STATE_OPEN_RUNNING), //
		OPEN_NOT_RUNNING_NOT_STARTED(SharkConstants.STATE_OPEN_NOT_RUNNING_NOT_STARTED), //
		OPEN_NOT_RUNNING_SUSPENDED(SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED), //
		CLOSED_COMPLETED(SharkConstants.STATE_CLOSED_COMPLETED), //
		CLOSED_TERMINATED(SharkConstants.STATE_CLOSED_TERMINATED), //
		CLOSED_ABORTED(SharkConstants.STATE_CLOSED_ABORTED), //
		UNKNOWN(StringUtils.EMPTY) //
		;

		private final String sharkRunningState;

		private RunningStates(final String sharkRunningState) {
			this.sharkRunningState = sharkRunningState;
		}

		public boolean isClosed() {
			return sharkRunningState.startsWith(SharkConstants.STATEPREFIX_CLOSED);
		}

		public static RunningStates fromSharkRunningState(final String sharkRunningState) {
			if (sharkRunningState != null) {
				for (final RunningStates runningStates : RunningStates.values()) {
					if (sharkRunningState.equals(runningStates.sharkRunningState)) {
						return runningStates;
					}
				}
			}
			return UNKNOWN;
		}

	}

	private CMEventManager eventManager;

	public SharkEventsDelegator() {
		setEventManager(new NullEventManager());
	}

	public SharkEventsDelegator(final CMEventManager eventManager) {
		setEventManager(eventManager);
	}

	public void setEventManager(final CMEventManager eventManager) {
		Validate.notNull(eventManager, "Manager cannot be null");
		this.eventManager = eventManager;
	}

	@Override
	public void persist(final WMSessionHandle shandle, final StateEventAuditPersistenceObject sea)
			throws EventAuditException {
		try {
			final EventType eventType = EventType.fromSharkEventType(sea.getType());
			switch (eventType) {
			case PROCESS_STATE_CHANGED:
				fireProcessStateChanged(shandle, sea);
				break;
			case ACTIVITY_STATE_CHANGED:
				fireActivityStateChanged(shandle, sea);
				break;
			}
		} catch (final Exception e) {
			throw new EventAuditException(e);
		}
	}

	@Legacy("State map copied from the old implementation. It does not work. We should track EVERY change except for OPEN_NOT_RUNNING_NOT_STARTED.")
	private void fireProcessStateChanged(final WMSessionHandle shandle, final StateEventAuditPersistenceObject sea) {
		final RunningStates oldState = RunningStates.fromSharkRunningState(sea.getOldState());
		final RunningStates newState = RunningStates.fromSharkRunningState(sea.getNewState());
		final String procDefId = sea.getProcessDefinitionId();
		if (oldState == RunningStates.OPEN_NOT_RUNNING_NOT_STARTED && newState == RunningStates.OPEN_RUNNING) {
			eventManager.processStarted(procDefId);
		} else if (newState.isClosed()) {
			eventManager.processClosed(procDefId);
		} else if (oldState == RunningStates.OPEN_RUNNING && newState == RunningStates.OPEN_NOT_RUNNING_SUSPENDED) {
			eventManager.processSuspended(procDefId);
		} else if (oldState == RunningStates.OPEN_NOT_RUNNING_SUSPENDED && newState == RunningStates.OPEN_RUNNING) {
			eventManager.processResumed(procDefId);
		}
	}

	@Legacy("State map copied from the old implementation.")
	private void fireActivityStateChanged(final WMSessionHandle shandle, final StateEventAuditPersistenceObject sea)
			throws Exception {
		final String actDefId = sea.getActivityDefinitionId();
		final RunningStates oldState = RunningStates.fromSharkRunningState(sea.getOldState());
		final RunningStates newState = RunningStates.fromSharkRunningState(sea.getNewState());
		switch (newState) {
		case CLOSED_COMPLETED:
			eventManager.activityClosed(actDefId);
			break;
		case OPEN_NOT_RUNNING_NOT_STARTED:
			if (oldState == RunningStates.UNKNOWN) {
				eventManager.activityStarted(actDefId);
			}
			break;
		}
		final WMEntity en = Shark.getInstance().getAdminMisc()
				.getActivityDefinitionInfo(shandle, sea.getProcessId(), sea.getActivityId());
		final WMFilter filter = new WMFilter("Name", WMFilter.EQ, "Performer");
		filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
		Shark.getInstance().getXPDLBrowser().listAttributes(shandle, en, filter, false);
		Shark.getInstance().getWAPIConnection()
				.listActivityInstanceAttributes(shandle, sea.getProcessId(), sea.getActivityId(), null, false);
	}

}
