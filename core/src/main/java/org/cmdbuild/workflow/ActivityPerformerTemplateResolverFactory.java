package org.cmdbuild.workflow;

import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.common.template.TemplateResolverImpl;
import org.cmdbuild.services.template.DatabaseTemplateEngine;

public class ActivityPerformerTemplateResolverFactory {

	private final DatabaseTemplateEngine databaseTemplateEngine;
	private final String prefix;

	public ActivityPerformerTemplateResolverFactory(final DatabaseTemplateEngine databaseTemplateEngine,
			final String prefix) {
		this.databaseTemplateEngine = databaseTemplateEngine;
		this.prefix = prefix;
	}

	public TemplateResolver create() {
		return TemplateResolverImpl.newInstance() //
				.withEngine(databaseTemplateEngine, prefix) //
				.build();
	}

}
