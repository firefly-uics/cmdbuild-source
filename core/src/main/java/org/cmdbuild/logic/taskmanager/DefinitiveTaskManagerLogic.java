package org.cmdbuild.logic.taskmanager;

public class DefinitiveTaskManagerLogic extends ForwardingTaskManagerLogic {

	private final TaskManagerLogic delegate;

	public DefinitiveTaskManagerLogic(final TaskManagerLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	protected TaskManagerLogic delegate() {
		return delegate;
	}

}
