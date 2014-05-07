package org.cmdbuild.logic.translation;

import java.util.Map;

import org.cmdbuild.services.store.EnabledLanguagesStore;
public class DefaultEnabledLanguagesLogic implements EnabledLanguagesLogic {
	
	final private EnabledLanguagesStore store;

	public DefaultEnabledLanguagesLogic(EnabledLanguagesStore store) {
		this.store = store;
	}
	
	@Override
	public Map<String, String> read() {
		return this.store.read();
	}

	@Override
	public void write(
			final Map<String, String> requestParams //
			) {
		this.store.write(requestParams);
	}

}
