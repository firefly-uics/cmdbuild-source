package org.cmdbuild.services.localization;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.dao.ForwardingStorableConverter;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.services.store.menu.LocalizedMenuElement;
import org.cmdbuild.services.store.menu.MenuElement;

import com.google.common.base.Function;

public class LocalizedStorableConverter<T extends Storable> extends ForwardingStorableConverter<T> {

	private final StorableConverter<T> delegate;
	private final TranslationFacade facade;
	private final CMDataView dataView;

	public LocalizedStorableConverter(final StorableConverter<T> delegate, final TranslationFacade facade,
			final CMDataView dataView) {
		this.delegate = delegate;
		this.facade = facade;
		this.dataView = dataView;
	}

	@Override
	protected StorableConverter<T> delegate() {
		return delegate;
	}

	@Override
	public T convert(final CMCard card) {
		return proxy(super.convert(card));
	}

	private T proxy(final T input) {
		final T output;
		if (input instanceof LocalizableStorable) {
			final LocalizableStorable localizedStorable = LocalizableStorable.class.cast(input);
			output = new LocalizableStorableVisitor() {

				private T output;

				public T proxy() {
					localizedStorable.accept(this);
					return output;
				}

				@Override
				public void visit(final Lookup storable) {
					output = (T) storable;
					output = new Function<Lookup, T>() {

						@Override
						public T apply(final Lookup input) {
							return (T) ((input == null) ? null : new LocalizedLookup(input, facade));
						}
					}.apply(storable);
				}

				@Override
				public void visit(final MenuElement storable) {
					output = (T) storable;
					output = new Function<MenuElement, T>() {

						@Override
						public T apply(final MenuElement input) {
							return (T) ((input == null) ? null : new LocalizedMenuElement(input, facade, dataView));
						}
					}.apply(storable);
				}
			}.proxy();
		} else {
			output = input;
		}
		return output;
	}

}
