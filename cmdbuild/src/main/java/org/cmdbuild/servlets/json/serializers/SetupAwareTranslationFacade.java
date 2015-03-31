package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;

public class SetupAwareTranslationFacade extends ForwardingTranslationFacade {

	private final TranslationFacade delegate;
	private final SetupFacade setupFacade;

	public SetupAwareTranslationFacade(final TranslationFacade delegate, final SetupFacade setupFacade) {
		this.delegate = delegate;
		this.setupFacade = setupFacade;
	}

	@Override
	protected TranslationFacade delegate() {
		return delegate;
	}

	@Override
	public String read(final TranslationObject translationObject) {
		return setupFacade.isEnabled() ? super.read(translationObject) : null;
	}

}
