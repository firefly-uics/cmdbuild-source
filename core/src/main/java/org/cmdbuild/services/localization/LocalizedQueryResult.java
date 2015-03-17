package org.cmdbuild.services.localization;

import java.util.NoSuchElementException;

import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.ForwardingQueryResult;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.translation.TranslationFacade;

import com.google.common.base.Function;

public class LocalizedQueryResult extends ForwardingQueryResult {

	private final CMQueryResult delegate;
	private final Function<CMQueryRow, CMQueryRow> TO_LOCALIZED_QUERYROW;

	public LocalizedQueryResult(final CMQueryResult delegate, final TranslationFacade facade,
			final LookupStore lookupStore) {
		this.delegate = delegate;
		this.TO_LOCALIZED_QUERYROW = new Function<CMQueryRow, CMQueryRow>() {

			@Override
			public CMQueryRow apply(final CMQueryRow input) {
				return (input == null) ? null : new LocalizedQueryRow(input, facade, lookupStore);
			}
		};
	}

	@Override
	protected CMQueryResult delegate() {
		return delegate;
	}

	@Override
	public CMQueryRow getOnlyRow() throws NoSuchElementException {
		return proxy(super.getOnlyRow());
	}

	private CMQueryRow proxy(final CMQueryRow row) {
		return TO_LOCALIZED_QUERYROW.apply(row);
	}

}
