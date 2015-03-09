package org.cmdbuild.services.localization;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.ForwardingDataView;
import org.cmdbuild.logic.translation.TranslationFacade;

import com.google.common.base.Function;

public class LocalizedDataView extends ForwardingDataView {

	private final CMDataView delegate;
	private final Function<CMClass, CMClass> TO_LOCALIZED_CLASS;

	public LocalizedDataView(final CMDataView delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.TO_LOCALIZED_CLASS = new Function<CMClass, CMClass>() {

			@Override
			public CMClass apply(final CMClass input) {
				return (input == null) ? null : new LocalizedClass(input, facade);
			}

		};
	}

	@Override
	protected CMDataView delegate() {
		return delegate;
	}

	@Override
	public CMClass findClass(final Long id) {
		return proxy(super.findClass(id));
	}

	@Override
	public CMClass findClass(final String name) {
		return proxy(super.findClass(name));
	}

	@Override
	public CMClass findClass(final CMIdentifier identifier) {
		return proxy(super.findClass(identifier));
	}

	@Override
	public Iterable<? extends CMClass> findClasses() {
		return proxy(super.findClasses());
	}

	@Override
	public CMClass create(final CMClassDefinition definition) {
		return proxy(super.create(definition));
	}

	@Override
	public CMClass update(final CMClassDefinition definition) {
		return proxy(super.update(definition));
	}

	@Override
	public CMClass getActivityClass() {
		return proxy(super.getActivityClass());
	}

	@Override
	public CMClass getReportClass() {
		return proxy(super.getReportClass());
	}

	CMClass proxy(final CMClass type) {
		return TO_LOCALIZED_CLASS.apply(type);
	}

	Iterable<CMClass> proxy(final Iterable<? extends CMClass> types) {
		return from(types) //
				.transform(TO_LOCALIZED_CLASS) //
				.filter(CMClass.class);
	}

}
