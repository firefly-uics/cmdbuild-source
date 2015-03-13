package org.cmdbuild.services.localization;

import org.cmdbuild.data.store.lookup._Lookup;
import org.cmdbuild.logic.translation.TranslationFacade;

public class LocalizedLookup implements LocalizedStorable {
	
	private final _Lookup delegate;
	private final TranslationFacade facade;

	public LocalizedLookup(_Lookup delegate, TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	public void accept(LocalizedStorableVisitor visitor) {
		visitor.visit(this);
	}

}
