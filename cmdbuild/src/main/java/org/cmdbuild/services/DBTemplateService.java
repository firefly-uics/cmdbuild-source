package org.cmdbuild.services;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.auth.UserContext;

/**
 * Monostate holding the templates defined in the database.
 * 
 * It currently uses the old DAO layer because the new one does not handle
 * simple classes.
 */
public class DBTemplateService implements TemplateRepository {

	private static final String TEMPLATES_TABLE = "_Templates";
	private static final String TEMPLATE_NAME = "Name";
	private static final String TEMPLATE_DEFINITION = "Template";

	private static volatile Map<String, String> templates; // Access through getTemplates()
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
		Map<String, String> newTemplates = new HashMap<String, String>();
		final ITable templatesTable = UserContext.systemContext().tables().get(TEMPLATES_TABLE);
		for (ICard templateCard : templatesTable.cards().list()) {
			final String name = templateCard.getAttributeValue(TEMPLATE_NAME).getString();
			final String definition = templateCard.getAttributeValue(TEMPLATE_DEFINITION).getString();
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
