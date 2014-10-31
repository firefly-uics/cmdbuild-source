package org.cmdbuild.servlets.json.serializers;

import static com.google.common.collect.Iterables.isEmpty;

import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationObject;

public class SetupAwareTranslationFacade extends ForwardingTranslationFacade {

	private final SetupFacade setupFacade;

	public SetupAwareTranslationFacade(final TranslationFacade delegate, final SetupFacade setupFacade) {
		super(delegate);
		this.setupFacade = setupFacade;
	}

	@Override
	public String read(final TranslationObject translationObject) {
		return isEmpty(setupFacade.getEnabledLanguages()) ? null : super.read(translationObject);
	}
}
