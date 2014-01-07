package org.cmdbuild.common.template;

public interface TemplateResolver {

	/**
	 * Evaluates the specified template with no specific engine.
	 * 
	 * @param template
	 * 
	 * @return the evaluated template.
	 */
	String simpleEval(String template);

}
