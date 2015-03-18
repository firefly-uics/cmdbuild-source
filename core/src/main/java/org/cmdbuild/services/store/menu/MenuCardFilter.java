package org.cmdbuild.services.store.menu;

import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.model._View;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

public class MenuCardFilter {

	private final CMDataView dataView;
	private final CMGroup group;
	private final Supplier<PrivilegeContext> privilegeContext;
	private final StorableConverter<_View> viewConverter;

	public MenuCardFilter( //
			final CMDataView dataView, //
			final CMGroup group, //
			final Supplier<PrivilegeContext> privilegeContext, //
			final StorableConverter<_View> viewConverter //
	) {
		this.dataView = dataView;
		this.group = group;
		this.privilegeContext = privilegeContext;
		this.viewConverter = viewConverter;
	}

	public Iterable<CMCard> filterReadableMenuCards(final Iterable<CMCard> notFilteredMenuCards) {
		final List<CMCard> readableCards = Lists.newArrayList();
		final MenuCardPredicateFactory predicateFactory = new MenuCardPredicateFactory( //
				dataView, //
				group, //
				privilegeContext, //
				viewConverter);

		for (final CMCard menuCard : notFilteredMenuCards) {
			final Predicate<CMCard> predicate = predicateFactory.getPredicate(menuCard);
			if (predicate.apply(menuCard)) {
				readableCards.add(menuCard);
			}
		}

		return readableCards;
	}

}
