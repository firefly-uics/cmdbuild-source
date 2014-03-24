package org.cmdbuild.logic.taskmanager;

import org.springframework.transaction.annotation.Transactional;

public class TransactionalTaskManagerLogic extends ForwardingTaskManagerLogic {

	public TransactionalTaskManagerLogic(final TaskManagerLogic delegate) {
		super(delegate);
	}

	@Override
	@Transactional
	public Long create(final Task task) {
		return super.create(task);
	}

	@Override
	@Transactional
	public void update(final Task task) {
		super.update(task);
	}

	@Override
	@Transactional
	public void delete(final Task task) {
		super.delete(task);
	}

	@Override
	@Transactional
	public void start(final Long id) {
		super.start(id);
	}

	@Override
	@Transactional
	public void stop(final Long id) {
		super.stop(id);
	}

}
