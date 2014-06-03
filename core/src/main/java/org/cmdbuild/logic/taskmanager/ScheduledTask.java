package org.cmdbuild.logic.taskmanager;

import java.util.Date;

public interface ScheduledTask extends Task {

	String getCronExpression();

	Date getLastExecution();

}
