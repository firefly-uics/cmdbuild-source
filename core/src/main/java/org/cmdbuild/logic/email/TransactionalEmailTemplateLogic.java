package org.cmdbuild.logic.email;

import org.springframework.transaction.annotation.Transactional;

public class TransactionalEmailTemplateLogic extends ForwardingEmailTemplateLogic {

	public TransactionalEmailTemplateLogic(final EmailTemplateLogic delegate) {
		super(delegate);
	}

	@Transactional
	@Override
	public Long create(final Template template) {
		return super.create(template);
	}

	@Transactional
	@Override
	public void update(final Template template) {
		super.update(template);
	}

	@Transactional
	@Override
	public void delete(final String name) {
		super.delete(name);
	}

}
