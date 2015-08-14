package org.cmdbuild.logic.translation;

import java.util.Map;

import com.google.common.collect.Maps;

public interface TranslationObject {

	TranslationObject INVALID = new TranslationObject() {
		
		@Override
		public Map<String, String> getTranslations() {
			return Maps.newHashMap();
		}
		
		@Override
		public void accept(TranslationObjectVisitor visitor) {
			//nothing to do
		}
	};

	void accept(TranslationObjectVisitor visitor);

	Map<String, String> getTranslations();
	
}
