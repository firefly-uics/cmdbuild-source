package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;

public class TranslationFacade {

	private final LanguageStore languageStore;
	private final TranslationLogic translationLogic;

	public TranslationFacade(final LanguageStore languageStore, final TranslationLogic translationLogic) {
		this.languageStore = languageStore;
		this.translationLogic = translationLogic;
	}
	
	public String read(TranslationObject translationObject){
		String language = languageStore.getLanguage();
		return translationLogic.read(translationObject).get(language);
	}

}
