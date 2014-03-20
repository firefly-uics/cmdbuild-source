package org.cmdbuild.logic.email.rules;

import org.cmdbuild.logic.email.rules.StartWorkflow.Configuration;
import org.cmdbuild.logic.email.rules.StartWorkflow.Mapper;

public class ForwardingConfiguration implements Configuration {

	private final Configuration configuration;

	public ForwardingConfiguration(final Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String getClassName() {
		return configuration.getClassName();
	}

	@Override
	public Mapper getMapper() {
		return configuration.getMapper();
	}

	@Override
	public boolean advance() {
		return configuration.advance();
	}

	@Override
	public boolean saveAttachments() {
		return configuration.saveAttachments();
	}

}
