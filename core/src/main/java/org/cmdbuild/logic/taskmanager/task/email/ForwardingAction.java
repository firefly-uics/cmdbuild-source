package org.cmdbuild.logic.taskmanager.task.email;

import org.cmdbuild.data.store.email.Email;

import com.google.common.collect.ForwardingObject;

abstract class ForwardingAction extends ForwardingObject implements Action {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingAction() {
	}

	@Override
	protected abstract Action delegate();

	@Override
	public void execute(final Email email) {
		delegate().execute(email);
	}

}