package org.cmdbuild.services.scheduler.reademail;

import org.cmdbuild.services.scheduler.reademail.StartWorkflow.Configuration;
import org.cmdbuild.services.scheduler.reademail.StartWorkflow.Mapper;

public abstract class ForwardingConfiguration implements Configuration {

	private final Configuration configuration;

	protected ForwardingConfiguration(final Configuration configuration) {
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
