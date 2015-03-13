package org.cmdbuild.services.localization;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.logic.translation.TranslationFacade;

public class LocalizedLookup implements LocalizedStorable {
	
	private final Lookup delegate;
	private final TranslationFacade facade;

	public LocalizedLookup(Lookup delegate, TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	public void accept(LocalizedStorableVisitor visitor) {
		visitor.visit(this);
	}

}
