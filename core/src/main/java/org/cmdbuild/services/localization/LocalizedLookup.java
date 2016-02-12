package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.cmdbuild.data.store.lookup.ForwardingLookup;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.LookupConverter;

public class LocalizedLookup extends ForwardingLookup {

	private final Lookup delegate;
	private final TranslationFacade facade;

	protected LocalizedLookup(final Lookup delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	protected Lookup delegate() {
		return delegate;
	}

	@Override
	public String getDescription() {
		final String output;
		final String uuid = uuid();
		if (isNotBlank(uuid)) {
			final TranslationObject translationObject = LookupConverter.of(LookupConverter.description()).create(uuid);
			output = defaultIfBlank(facade.read(translationObject), super.getDescription());
		} else {
			output = super.getDescription();
		}
		return output;
	}

	@Override
	public String description() {
		return getDescription();
	}

}
