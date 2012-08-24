package org.cmdbuild.workflow;

import org.cmdbuild.services.soap.AbstractWorkflowEvent;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.ProcessStartEvent;
import org.cmdbuild.services.soap.ProcessUpdateEvent;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

/**
 * EventAuditManager that notifies CMDBuild through web services.
 */
public class CMDBuildEventAuditManager extends DelegatingEventAuditManager {

	static class WSEventNotifier implements SimpleEventManager {

		private final Private proxy;

		protected WSEventNotifier(final Private proxy) {
			this.proxy = proxy;
		}

		@Override
		public void processStarted(final ProcessInstance processInstance) {
			sendProcessStartEvent(processInstance);
		}

		@Override
		public void processClosed(final ProcessInstance processInstance) {
			sendProcessUpdateEvent(processInstance);
		}

		@Override
		public void processSuspended(final ProcessInstance processInstance) {
			sendProcessUpdateEvent(processInstance);
		}

		@Override
		public void processResumed(final ProcessInstance processInstance) {
			sendProcessUpdateEvent(processInstance);
		}

		@Override
		public void activityStarted(final ActivityInstance activityInstance) {
			sendProcessUpdateEventIfNoImpl(activityInstance);
		}

		@Override
		public void activityClosed(final ActivityInstance activityInstance) {
			sendProcessUpdateEventIfNoImpl(activityInstance);
		}

		private void sendProcessUpdateEventIfNoImpl(final ActivityInstance activityInstance) {
			if (activityInstance.isNoImplementationActivity()) {
				sendProcessUpdateEvent(activityInstance);
			}
		}

		private void sendProcessUpdateEvent(final ProcessInstance processInstance) {
			final AbstractWorkflowEvent event = new ProcessUpdateEvent();
			fillEventProperties(processInstance, event);
			proxy.notify(event);
		}

		private void sendProcessStartEvent(final ProcessInstance processInstance) {
			final AbstractWorkflowEvent event = new ProcessStartEvent();
			fillEventProperties(processInstance, event);
			proxy.notify(event);
		}

		private void fillEventProperties(final ProcessInstance processInstance,
				final AbstractWorkflowEvent workflowEvent) {
			final int sessionId = processInstance.getSessionHandle().getId();
			workflowEvent.setSessionId(sessionId);
			workflowEvent.setProcessDefinitionId(processInstance.getProcessDefinitionId());
			workflowEvent.setProcessInstanceId(processInstance.getProcessInstanceId());
		}
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		final Private proxy = new CusSoapProxyBuilder(cus).build();
		setEventManager(new WSEventNotifier(proxy));
	}

}
