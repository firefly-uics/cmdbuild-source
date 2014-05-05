package org.cmdbuild.spring.configuration;

import org.cmdbuild.logic.translation.DefaultEnabledLanguagesLogic;
import org.cmdbuild.logic.translation.EnabledLanguagesLogic;
import org.cmdbuild.services.store.EnabledLanguagesStore;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class EnabledLanguages {

	@Autowired
	private Data data;

	@Bean
	public EnabledLanguagesLogic enabledLanguagesLogic() {
		return new DefaultEnabledLanguagesLogic(enabledLanguagesStore());
	}

	@Bean
	protected EnabledLanguagesStore enabledLanguagesStore() {
		return new EnabledLanguagesStore(data.systemDataView());
	}
}
