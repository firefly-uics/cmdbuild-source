package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.logic.Logic;

/**
 * Handles all Task Manager operations.
 */
public interface TaskManagerLogic extends Logic {

	/**
	 * Adds a new {@link Task}.
	 */
	Long add(Task task);

	/**
	 * Reads all tasks.
	 */
	Iterable<? extends Task> readAll();

	/**
	 * Modifies an existing {@link Task}.
	 */
	void modify(Task task);

	/**
	 * Deletes an existing {@link Task}.
	 */
	void delete(Task task);

}
