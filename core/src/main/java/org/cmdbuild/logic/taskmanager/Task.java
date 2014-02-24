package org.cmdbuild.logic.taskmanager;

public interface Task {

	void accept(TaskVistor visitor);

	String getName();

	String getDescription();

	boolean isActive();

}
