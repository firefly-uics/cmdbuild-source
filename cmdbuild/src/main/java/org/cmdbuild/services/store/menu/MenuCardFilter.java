package org.cmdbuild.services.store.menu;

import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class MenuCardFilter {

	private final CMGroup group;

	public MenuCardFilter(final CMGroup group) {
		this.group = group;
	}

	public Iterable<CMCard> filterReadableMenuCards(final Iterable<CMCard> notFilteredMenuCards) {
		final List<CMCard> readableCards = Lists.newArrayList();
		final CMDataView view = TemporaryObjectsBeforeSpringDI.getSystemView();
		final MenuCardPredicateFactory predicateFactory = new MenuCardPredicateFactory(view, group);
		for (final CMCard menuCard : notFilteredMenuCards) {
			final Predicate<CMCard> predicate = predicateFactory.getPredicate(menuCard);
			if (predicate.apply(menuCard)) {
				readableCards.add(menuCard);
			}
		}
		return readableCards;
	}

}
