package org.cmdbuild.common.template;

public interface TemplateResolverEngine {

	/**
	 * Evaluates the specified expression.
	 * 
	 * @param expression
	 * 
	 * @return the result of the evaluation process, {@code null} if evaluation
	 *         was unsuccessful.
	 */
	Object eval(String expression);

}
