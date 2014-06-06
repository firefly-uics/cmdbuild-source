package org.cmdbuild.logic.taskmanager.task.email;

import org.cmdbuild.model.email.Email;

interface Action {

	void execute(Email email);

}