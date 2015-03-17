package org.cmdbuild.services.localization;

import org.cmdbuild.data.store.lookup.ForwardingLookup;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.LookupConverter;

import static org.apache.commons.lang3.StringUtils.*;

public class LocalizedLookup extends ForwardingLookup {

	private final Lookup delegate;
	private final TranslationFacade facade;

	public LocalizedLookup(final Lookup delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	public void accept(final LocalizableStorableVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected Lookup delegate() {
		return delegate;
	}
	
	@Override
	public String getDescription() {
		TranslationObject translationObject = LookupConverter.of(LookupConverter.description()).create(uuid());
		String translatedDescription = facade.read(translationObject);
		return defaultIfBlank(translatedDescription,super.getDescription());
	}
	
	
	@Override
	public String description() {
		return getDescription();
	}

}
