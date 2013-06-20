package org.cmdbuild.services.store.menu;

import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class MenuCardFilter {

	private final CMDataView dataView;
	private final CMGroup group;
	private final PrivilegeContextFactory privilegeContextFactory;

	public MenuCardFilter(final CMDataView dataView, final CMGroup group,
			final PrivilegeContextFactory privilegeContextFactory) {
		this.dataView = dataView;
		this.group = group;
		this.privilegeContextFactory = privilegeContextFactory;
	}

	public Iterable<CMCard> filterReadableMenuCards(final Iterable<CMCard> notFilteredMenuCards) {
		final List<CMCard> readableCards = Lists.newArrayList();
		final MenuCardPredicateFactory predicateFactory = new MenuCardPredicateFactory( //
				dataView, //
				group, //
				privilegeContextFactory //
			);

		for (final CMCard menuCard : notFilteredMenuCards) {
			final Predicate<CMCard> predicate = predicateFactory.getPredicate(menuCard);
			if (predicate.apply(menuCard)) {
				readableCards.add(menuCard);
			}
		}

		return readableCards;
	}

}
