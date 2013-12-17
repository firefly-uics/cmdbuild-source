package org.cmdbuild.services.template;

import org.cmdbuild.common.template.TemplateResolverEngine;
import org.cmdbuild.services.template.store.TemplateRepository;

public class DatabaseTemplateEngine implements TemplateResolverEngine {

	private final TemplateRepository templateRepository;

	public DatabaseTemplateEngine(final TemplateRepository templateRepository) {
		this.templateRepository = templateRepository;
	}

	@Override
	public Object eval(String expression) {
		return templateRepository.getTemplate(expression);
	}

}
