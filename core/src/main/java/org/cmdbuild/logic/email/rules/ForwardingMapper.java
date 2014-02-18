package org.cmdbuild.logic.email.rules;

import org.cmdbuild.logic.email.rules.StartWorkflow.Mapper;

public class ForwardingMapper implements Mapper {

	private final Mapper mapper;

	public ForwardingMapper(final Mapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Object getValue(final String name) {
		return mapper.getValue(name);
	}

}
