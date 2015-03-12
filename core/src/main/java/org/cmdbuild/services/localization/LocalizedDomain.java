package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.ForwardingDomain;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.converter.DomainConverter;

class LocalizedDomain extends ForwardingDomain {

	private final CMDomain delegate;
	private final TranslationFacade facade;
	private static final String DESCRIPTION = "Description";
	private static final String DIRECT_DESCRIPTION = "DirectDescription";
	private static final String INVERSE_DESCRIPTION = "InverseDescription";
	private static final String MASTERDETAIL_LABEL = "MasterDetail";

	LocalizedDomain(final CMDomain delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	protected CMDomain delegate() {
		return delegate;
	}

	@Override
	public String getDescription() {

		return defaultIfBlank( //
				facade.read(DomainConverter.of(DESCRIPTION) //
						.create(getName())), //
				super.getDescription());
	}

	@Override
	public String getDescription1() {
		return defaultIfBlank( //
				facade.read(DomainConverter.of(DIRECT_DESCRIPTION) //
						.create(getName())), //
				super.getDescription1());
	}

	@Override
	public String getDescription2() {
		return defaultIfBlank( //
				facade.read(DomainConverter.of(INVERSE_DESCRIPTION) //
						.create(getName())), //
				super.getDescription2());
	}

}
