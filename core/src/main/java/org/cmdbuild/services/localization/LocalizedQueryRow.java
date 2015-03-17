package org.cmdbuild.services.localization;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.ForwardingQueryRow;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.translation.TranslationFacade;

import com.google.common.base.Function;

public class LocalizedQueryRow extends ForwardingQueryRow {

	private final CMQueryRow delegate;
	private final Function<CMCard, CMCard> TO_LOCALIZED_CARD;

	public LocalizedQueryRow(final CMQueryRow delegate, final TranslationFacade facade, final LookupStore lookupStore) {
		this.delegate = delegate;
		this.TO_LOCALIZED_CARD = new Function<CMCard, CMCard>() {
			@Override
			public CMCard apply(final CMCard input) {
				return (input == null) ? null : new LocalizedCard(input, facade, lookupStore);
			}
		};
	}

	@Override
	protected CMQueryRow delegate() {
		return delegate;
	}

	@Override
	public CMCard getCard(final Alias alias) {
		return proxy(super.getCard(alias));
	}

	@Override
	public CMCard getCard(final CMClass type) {
		return proxy(super.getCard(type));
	}

	private CMCard proxy(final CMCard card) {
		return TO_LOCALIZED_CARD.apply(card);
	}

	@Override
	public QueryRelation getRelation(final Alias alias) {
		// TODO Auto-generated method stub
		return super.getRelation(alias);
	}

	@Override
	public QueryRelation getRelation(final CMDomain type) {
		// TODO Auto-generated method stub
		return super.getRelation(type);
	}

}
