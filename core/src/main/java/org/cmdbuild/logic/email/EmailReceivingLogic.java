package org.cmdbuild.logic.email;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.DefaultEmailCallbackHandler;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailCallbackHandler.RuleAction;
import org.cmdbuild.services.email.EmailService;

/**
 * This {@link Logic} component has been temporary created just to cut the
 * circular dependency between {@link EmailLogic} and
 * {@link DefaultWorkflowLogic}.
 */
public class EmailReceivingLogic implements Logic {

	private final EmailService service;
	private final Iterable<Rule> rules;
	private final Notifier notifier;

	public EmailReceivingLogic( //
			final EmailService service, //
			final Iterable<Rule> rules, //
			final Notifier notifier //
	) {
		this.service = service;
		this.rules = rules;
		this.notifier = notifier;
	}

	public void receive() {
		try {
			final DefaultEmailCallbackHandler callbackHandler = DefaultEmailCallbackHandler.of(rules);
			service.receive(callbackHandler);

			logger.info("executing actions");
			for (final RuleAction action : callbackHandler.getActions()) {
				try {
					action.execute();
				} catch (final Exception e) {
					logger.warn("error executing action");
				}
			}
		} catch (final CMDBException e) {
			notifier.warn(e);
		}
	}

}
