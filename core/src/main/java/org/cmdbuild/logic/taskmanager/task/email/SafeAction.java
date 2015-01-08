package org.cmdbuild.logic.taskmanager.task.email;

import org.cmdbuild.data.store.email.Email;
import org.cmdbuild.logic.Logic;
import org.slf4j.Logger;

class SafeAction extends ForwardingAction {

	public static SafeAction of(final Action delegate) {
		return new SafeAction(delegate);
	}

	private static final Logger logger = Logic.logger;

	private final Action delegate;

	private SafeAction(final Action delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Action delegate() {
		return delegate;
	}

	@Override
	public void execute(final Email email) {
		try {
			delegate().execute(email);
		} catch (final Throwable e) {
			logger.error("error executing action", e);
		}
	}

}