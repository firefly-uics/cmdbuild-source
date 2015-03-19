package org.cmdbuild.services.localization;

import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.services.store.report.ForwardingReport;
import org.cmdbuild.services.store.report.Report;

public class LocalizedReport extends ForwardingReport implements Report {

	private final Report delegate;
	private final TranslationFacade facade;

	LocalizedReport(final Report delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.facade = facade;
	}

	@Override
	protected Report delegate() {
		return delegate;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return super.getDescription();
	}

}
