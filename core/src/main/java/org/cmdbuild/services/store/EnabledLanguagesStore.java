package org.cmdbuild.services.store;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.view.CMDataView;

public class EnabledLanguagesStore {

	static Map<String, String> translations = new HashMap<String, String>();
	
	private final CMDataView dataView;

	public EnabledLanguagesStore(final CMDataView dataView) {
		this.dataView = dataView;
	}
	
	public Map<String, String> read() {
		return translations;
	}

	public void write(
			final Map<String, String> requestParams //
			) {
		for (final Object keyObject : requestParams.keySet()) {
			final String key = keyObject.toString();
			String value = requestParams.get(key);
			translations.put(key, value);
		}
	}
}
