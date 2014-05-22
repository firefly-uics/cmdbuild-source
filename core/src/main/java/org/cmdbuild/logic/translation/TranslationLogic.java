package org.cmdbuild.logic.translation;

import java.util.Map;

import org.cmdbuild.logic.Logic;

public interface TranslationLogic extends Logic {

	void create(TranslationObject translationObject);

	Map<String, String> read(TranslationObject translationObject);

	void update(TranslationObject translationObject);

	void delete(TranslationObject translationObject);

}
