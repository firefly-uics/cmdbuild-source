package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.logic.Logic;

/**
 * Handles all Task Manager operations.
 */
public interface TaskManagerLogic extends Logic {

	/**
	 * Adds a new {@link Task}.
	 */
	void add(Task task);

	/**
	 * Deletes an existing {@link Task}.
	 */
	void delete(Task task);

}
