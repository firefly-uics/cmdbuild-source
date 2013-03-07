package org.cmdbuild.services;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.CardDTO;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.FetchCardListResponse;

/**
 * Monostate holding the templates defined in the database.
 */
public class DBTemplateService implements TemplateRepository {

	private static final String TEMPLATES_TABLE = "_Templates";
	private static final String TEMPLATE_NAME = "Name";
	private static final String TEMPLATE_DEFINITION = "Template";
	private static volatile Map<String, String> templates;
	private static final Object templatesLock = new Object();

	private Map<String, String> getTemplatesMap() {
		if (templates == null) {
			synchronized (templatesLock) {
				if (templates == null) {
					initTemplates();
				}
			}
		}
		return templates;
	}

	private void initTemplates() {
		final Map<String, String> newTemplates = new HashMap<String, String>();
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final FetchCardListResponse response = dataAccessLogic.fetchCards(TEMPLATES_TABLE, QueryOptions
				.newQueryOption().build());
		for (final CardDTO templateCard : response.getPaginatedCards()) {
			final String name = (String) templateCard.getAttribute(TEMPLATE_NAME);
			final String definition = (String) templateCard.getAttribute(TEMPLATE_DEFINITION);
			newTemplates.put(name, definition);
		}
		templates = newTemplates;
	}

	@Override
	public String getTemplate(final String name) {
		return getTemplatesMap().get(name);
	}

	// Should be notified, not called directly
	public void reload() {
		initTemplates();
	}
}
