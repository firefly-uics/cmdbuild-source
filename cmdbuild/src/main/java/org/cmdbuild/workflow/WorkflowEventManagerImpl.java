package org.cmdbuild.workflow;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.event.WorkflowEvent;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;

/**
 * Workflow event manager that uses the legacy persistence layer.
 */
@Legacy("Not tested")
public class WorkflowEventManagerImpl extends LegacyWorkflowPersistence implements WorkflowEventManager {

	private static EventMap EMPTY_EVENT_MAP = new EventMap();

	private static class EventMap implements Iterable<WorkflowEvent> {
		private final Map<String, WorkflowEvent> events = new HashMap<String, WorkflowEvent>();

		public void push(final WorkflowEvent event) {
			final String processInstanceId = event.getProcessInstanceId();
			if (!events.containsKey(processInstanceId)) {
				// start events must not be overridden by
				// updates, and they always come first!
				events.put(processInstanceId, event);
			}
		}

		@Override
		public Iterator<WorkflowEvent> iterator() {
			return events.values().iterator();
		}

	}

	private static class SessionEventMap {
		private final Map<Integer, EventMap> sessionEvents = new HashMap<Integer, EventMap>();

		public void pushEvent(final int sessionId, final WorkflowEvent event) {
			EventMap eventMap = sessionEvents.get(sessionId);
			if (eventMap == null) {
				eventMap = new EventMap();
				sessionEvents.put(sessionId, eventMap);
			}
			eventMap.push(event);
		}

		public Iterable<WorkflowEvent> pullEvents(final int sessionId) {
			final EventMap eventMap = sessionEvents.get(sessionId);
			if (eventMap != null) {
				return eventMap;
			} else {
				return EMPTY_EVENT_MAP;
			}
		}
	}

	private SessionEventMap sessionEventMap;

	public WorkflowEventManagerImpl( //
			final CMWorkflowService workflowService, //
			final WorkflowTypesConverter variableConverter, //
			final ProcessDefinitionManager processDefinitionManager) {
		super(UserContext.systemContext(), workflowService, variableConverter, processDefinitionManager);
		sessionEventMap = new SessionEventMap();
	}

	@Override
	public synchronized void pushEvent(final int sessionId, WorkflowEvent event) {
		sessionEventMap.pushEvent(sessionId, event);
	}

	@Override
	public synchronized void processEvents(final int sessionId) throws CMWorkflowException {
		Log.WORKFLOW.info(format("processing events for session '%s'", sessionId));
		for (WorkflowEvent event : sessionEventMap.pullEvents(sessionId)) {
			final WSProcessInstInfo procInstInfo = workflowService.getProcessInstance(event.getProcessInstanceId());
			final CMProcessInstance processInstance = findOrCreateProcessInstance(event, procInstInfo);
			if (processInstance != null) {
				// process not found on the database
				syncProcessStateActivitiesAndVariables(processInstance, procInstInfo);
			}
		}
		purgeEvents(sessionId);
	}

	private WSProcessInstInfo fakeClosedProcessInstanceInfo(final WorkflowEvent event) throws CMWorkflowException {
		return new WSProcessInstInfo() {

			@Override
			public String getProcessDefinitionId() {
				return event.getProcessDefinitionId();
			}

			@Override
			public String getPackageId() {
				throw new UnsupportedOperationException("No information");
			}

			@Override
			public String getPackageVersion() {
				throw new UnsupportedOperationException("No information");
			}

			@Override
			public String getProcessInstanceId() {
				return event.getProcessInstanceId();
			}

			@Override
			public WSProcessInstanceState getStatus() {
				return WSProcessInstanceState.COMPLETED;
			}
		};
	}

	private CMProcessInstance findOrCreateProcessInstance(final WorkflowEvent event, WSProcessInstInfo procInstInfo) throws CMWorkflowException {
		switch (event.getType()) {
		case START:
			return createProcessInstance(procInstInfo);
		case UPDATE:
			if (procInstInfo == null) {
				// closed processes are removed from shark
				procInstInfo = fakeClosedProcessInstanceInfo(event);
			}
			return findProcessInstance(procInstInfo);
		default:
			throw new IllegalArgumentException("Invalid event type");
		}
	}

	@Override
	public synchronized void purgeEvents(final int sessionId) {
		sessionEventMap.pullEvents(sessionId);
	}
}
