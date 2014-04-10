package org.cmdbuild.logic.email.rules;

import org.cmdbuild.logic.email.rules.StartWorkflow.Mapper;

public abstract class ForwardingMapper implements Mapper {

	private final Mapper mapper;

	protected ForwardingMapper(final Mapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Object getValue(final String name) {
		return mapper.getValue(name);
	}

}
