package org.cmdbuild.logic.taskmanager.task.email;

import org.cmdbuild.data.store.email.Email;

abstract class ForwardingAction implements Action {

	private final Action delegate;

	protected ForwardingAction(final Action delegate) {
		this.delegate = delegate;
	}

	@Override
	public void execute(final Email email) {
		delegate.execute(email);
	}

}