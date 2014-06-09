package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;

public class DefaultTranslationFacade implements TranslationFacade {

	private final LanguageStore languageStore;
	private final TranslationLogic translationLogic;

	public DefaultTranslationFacade(final LanguageStore languageStore, final TranslationLogic translationLogic) {
		this.languageStore = languageStore;
		this.translationLogic = translationLogic;
	}

	@Override
	public String read(final TranslationObject translationObject) {
		final String language = languageStore.getLanguage();
		return translationLogic.read(translationObject).get(language);
	}

}
