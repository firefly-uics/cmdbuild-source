package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.ForwardingClass;
import org.cmdbuild.logic.translation.ClassDescription.ClassDescriptionConverter;
import org.cmdbuild.logic.translation.TranslationFacade;

class LocalizedClass extends ForwardingClass {

	private final CMClass delegate;
	private final TranslationFacade facade;
	private static final String DESCRIPTION = "Description";

	LocalizedClass(final CMClass delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	protected CMClass delegate() {
		return delegate;
	}

	@Override
	public String getDescription() {

		return defaultIfBlank( //
				facade.read(ClassDescriptionConverter.of(DESCRIPTION) //
						.create(getName())), //
				super.getDescription());
	}

}
