package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.logic.translation.TranslationObject;

public abstract class ForwardingTranslationFacade implements TranslationFacade {

	private final TranslationFacade delegate;

	protected ForwardingTranslationFacade(final TranslationFacade delegate) {
		this.delegate = delegate;
	}

	@Override
	public String read(final TranslationObject translationObject) {
		return delegate.read(translationObject);
	}

}
