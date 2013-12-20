package org.cmdbuild.utils.template;

public interface TemplateResolver {

	/**
	 * Evaluate an external template string with no specific engine
	 * 
	 * @param template
	 * @return expanded template
	 */
	String simpleEval(String template);

}
