package org.cmdbuild.logic.taskmanager;

import org.springframework.transaction.annotation.Transactional;

public class TransactionalScheduledTaskFacade extends ForwardingScheduledTaskFacade {

	public TransactionalScheduledTaskFacade(final ScheduledTaskFacade delegate) {
		super(delegate);
	}

	@Override
	@Transactional
	public Long create(final ScheduledTask task) {
		return super.create(task);
	}

	@Override
	@Transactional
	public void update(final ScheduledTask task) {
		super.update(task);
	}

	@Override
	@Transactional
	public void delete(final ScheduledTask task) {
		super.delete(task);
	}

}
