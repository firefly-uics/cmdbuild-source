package org.cmdbuild.services.localization;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.logic.translation.TranslationFacade;

public class LocalizedLookup implements LocalizedStorable {

	private final Lookup delegate;
	private final TranslationFacade facade;

	public LocalizedLookup(final Lookup delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	public void accept(final LocalizedStorableVisitor visitor) {
		visitor.visit(this);
	}

}
