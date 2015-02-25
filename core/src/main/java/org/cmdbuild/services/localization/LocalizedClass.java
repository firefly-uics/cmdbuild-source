package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.DESCRIPTION_FOR_CLIENT;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.ForwardingClass;
import org.cmdbuild.logic.translation.ClassTranslation;
import org.cmdbuild.logic.translation.TranslationFacade;

class LocalizedClass extends ForwardingClass {

	private final CMClass delegate;
	private final TranslationFacade facade;

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
				facade.read(ClassTranslation.newInstance() //
						.withName(getName()) //
						.withField(DESCRIPTION_FOR_CLIENT) //
						.build()), //
				super.getDescription());
	}

}
