package org.cmdbuild.logic.taskmanager.task.email;

import org.cmdbuild.data.store.email.Email;

interface Action {

	void execute(Email email);

}