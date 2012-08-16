package org.cmdbuild.workflow;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.common.annotations.Legacy;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.api.internal.eventaudit.EventAuditException;
import org.enhydra.shark.api.internal.eventaudit.EventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.StateEventAuditPersistenceObject;

public class SharkEventsDelegator extends NullEventAuditManager {

	private static class EventAuditPersistenceObjectWrapper implements CMEventManager.ActivityInstance {

		private final EventAuditPersistenceObject eap;

		private EventAuditPersistenceObjectWrapper(final EventAuditPersistenceObject eap) {
			this.eap = eap;
		}

		@Override
		public String getProcessDefinitionId() {
			return eap.getProcessDefinitionId();
		}

		@Override
		public String getProcessInstanceId() {
			return eap.getProcessId();
		}

		@Override
		public String getActivityDefinitionId() {
			return eap.getActivityDefinitionId();
		}

		@Override
		public String getActivityInstanceId() {
			return eap.getActivityId();
		}
	}

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
		if (oldState == RunningStates.OPEN_NOT_RUNNING_NOT_STARTED && newState == RunningStates.OPEN_RUNNING) {
			eventManager.processStarted(processInstanceFor(sea));
		} else if (newState.isClosed()) {
			eventManager.processClosed(processInstanceFor(sea));
		} else if (oldState == RunningStates.OPEN_RUNNING && newState == RunningStates.OPEN_NOT_RUNNING_SUSPENDED) {
			eventManager.processSuspended(processInstanceFor(sea));
		} else if (oldState == RunningStates.OPEN_NOT_RUNNING_SUSPENDED && newState == RunningStates.OPEN_RUNNING) {
			eventManager.processResumed(processInstanceFor(sea));
		}
	}

	private CMEventManager.ProcessInstance processInstanceFor(final EventAuditPersistenceObject eap) {
		return new EventAuditPersistenceObjectWrapper(eap);
	}

	@Legacy("State map copied from the old implementation.")
	private void fireActivityStateChanged(final WMSessionHandle shandle, final StateEventAuditPersistenceObject sea)
			throws Exception {
		final RunningStates oldState = RunningStates.fromSharkRunningState(sea.getOldState());
		final RunningStates newState = RunningStates.fromSharkRunningState(sea.getNewState());
		switch (newState) {
		case CLOSED_COMPLETED:
			eventManager.activityClosed(activityInstanceFor(sea));
			break;
		case OPEN_NOT_RUNNING_NOT_STARTED:
			if (oldState == RunningStates.UNKNOWN) {
				eventManager.activityStarted(activityInstanceFor(sea));
			}
			break;
		}
	}

	private CMEventManager.ActivityInstance activityInstanceFor(final EventAuditPersistenceObject eap) {
		return new EventAuditPersistenceObjectWrapper(eap);
	}
}
