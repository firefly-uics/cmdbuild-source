package org.cmdbuild.logic.email;

import java.util.Map;

import org.cmdbuild.logic.Logic;

public interface EmailTemplateLogic extends Logic {

	interface Template {

		Long getId();

		String getName();

		String getDescription();

		String getFrom();

		String getTo();

		String getCc();

		String getBcc();

		String getSubject();

		String getBody();

		Map<String, String> getVariables();

		Long getAccount();

	}

	/**
	 * Reads all {@link Template}s.
	 */
	Iterable<Template> readAll();

	/**
	 * Reads the {@link Template} with the specified name.
	 */
	Template read(String name);

	/**
	 * Creates a new {@link Template}.
	 * 
	 * @return the id of the created {@link Template}.
	 */
	Long create(final Template template);

	/**
	 * Updates the given {@link Template}.
	 */
	void update(final Template template);

	/**
	 * Remove the {@link Template} with the given name.
	 */
	void delete(final String name);

}
