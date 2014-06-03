package org.cmdbuild.spring.configuration;

import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.StoreFactory;
import org.cmdbuild.data.store.translation.TranslationConverter;
import org.cmdbuild.logic.translation.DefaultTranslationLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class Translation {

	@Autowired
	private Data data;

	@Bean
	public TranslationLogic translationLogic() {
		return new DefaultTranslationLogic(translationStoreFactory());
	}

	@Bean
	protected StoreFactory<org.cmdbuild.data.store.translation.Translation> translationStoreFactory() {
		return new StoreFactory<org.cmdbuild.data.store.translation.Translation>() {

			@Override
			public Store<org.cmdbuild.data.store.translation.Translation> create(final Groupable groupable) {
				return DataViewStore.newInstance(data.systemDataView(), groupable, translationConverter());
			}

		};
	}

	@Bean
	protected TranslationConverter translationConverter() {
		return new TranslationConverter();
	}

}
