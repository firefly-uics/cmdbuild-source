package org.cmdbuild.logic.taskmanager;

public interface ScheduledTask extends Task {

	Long getId();

	String getCronExpression();

}
