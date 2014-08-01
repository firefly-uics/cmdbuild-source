package org.cmdbuild.logic.taskmanager;

public class DefinitiveTaskManagerLogic extends ForwardingTaskManagerLogic {

	public DefinitiveTaskManagerLogic(final TaskManagerLogic delegate) {
		super(delegate);
	}

}
