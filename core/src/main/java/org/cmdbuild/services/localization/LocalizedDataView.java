package org.cmdbuild.services.localization;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.ForwardingDataView;
import org.cmdbuild.logic.translation.TranslationFacade;

import com.google.common.base.Function;

public class LocalizedDataView extends ForwardingDataView {

	private final CMDataView delegate;
	private final Function<CMClass, CMClass> TO_LOCALIZED_CLASS;
	private final Function<CMDomain, CMDomain> TO_LOCALIZED_DOMAIN;

	public LocalizedDataView(final CMDataView delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.TO_LOCALIZED_CLASS = new Function<CMClass, CMClass>() {

			@Override
			public CMClass apply(final CMClass input) {
				return (input == null) ? null : new LocalizedClass(input, facade);
			}

		};
		this.TO_LOCALIZED_DOMAIN = new Function<CMDomain, CMDomain>() {

			@Override
			public CMDomain apply(final CMDomain input) {
				return (input == null) ? null : new LocalizedDomain(input, facade);
			}

		};
	}

	@Override
	protected CMDataView delegate() {
		return delegate;
	}

	@Override
	public CMClass findClass(final Long id) {
		return proxyClass(super.findClass(id));
	}

	@Override
	public CMClass findClass(final String name) {
		return proxyClass(super.findClass(name));
	}

	@Override
	public CMClass findClass(final CMIdentifier identifier) {
		return proxyClass(super.findClass(identifier));
	}

	@Override
	public Iterable<? extends CMClass> findClasses() {
		return proxyClasses(super.findClasses());
	}

	@Override
	public CMClass create(final CMClassDefinition definition) {
		return proxyClass(super.create(definition));
	}

	@Override
	public CMClass update(final CMClassDefinition definition) {
		return proxyClass(super.update(definition));
	}

	@Override
	public CMClass getActivityClass() {
		return proxyClass(super.getActivityClass());
	}

	@Override
	public CMClass getReportClass() {
		return proxyClass(super.getReportClass());
	}
	
	@Override
	public CMDomain findDomain(final Long id) {
		return proxyDomain(super.findDomain(id));
	}

	@Override
	public CMDomain findDomain(final String name) {
		return proxyDomain(super.findDomain(name));
	}

	@Override
	public CMDomain findDomain(final CMIdentifier identifier) {
		return proxyDomain(super.findDomain(identifier));
	}

	@Override
	public Iterable<? extends CMDomain> findDomains() {
		return proxyDomains(super.findDomains());
	}

	@Override
	public Iterable<? extends CMDomain> findDomainsFor(final CMClass type) {
		return proxyDomains(super.findDomainsFor(type));
	}

	CMClass proxyClass(final CMClass type) {
		return TO_LOCALIZED_CLASS.apply(type);
	}

	Iterable<CMClass> proxyClasses(final Iterable<? extends CMClass> types) {
		return from(types) //
				.transform(TO_LOCALIZED_CLASS) //
				.filter(CMClass.class);
	}
	
	CMDomain proxyDomain(final CMDomain type) {
		return TO_LOCALIZED_DOMAIN.apply(type);
	}

	Iterable<CMDomain> proxyDomains(final Iterable<? extends CMDomain> types) {
		return from(types) //
				.transform(TO_LOCALIZED_DOMAIN) //
				.filter(CMDomain.class);
	}

}
