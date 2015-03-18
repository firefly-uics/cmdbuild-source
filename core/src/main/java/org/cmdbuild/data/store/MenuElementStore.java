package org.cmdbuild.data.store;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.services.store.menu.MenuConstants.GROUP_NAME_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.MENU_CLASS_NAME;

import java.util.List;

import org.cmdbuild.auth.GroupFetcher;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.model.View;
import org.cmdbuild.services.store.menu.MenuCardFilter;
import org.cmdbuild.services.store.menu.MenuElement;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MenuElementStore extends ForwardingStore<MenuElement> {

	private final Store<MenuElement> delegate;
	private final CMDataView dataView;
	private final OperationUser operationUser;
	private final StorableConverter<View> viewConverter;
	private final Function<CMCard, MenuElement> CONVERT;

	public MenuElementStore(final Store<MenuElement> delegate, final CMDataView dataView,
			final OperationUser operationUser, final StorableConverter<View> viewConverter,
			final StorableConverter<MenuElement> converter) {
		this.delegate = delegate;
		this.dataView = dataView;
		this.operationUser = operationUser;
		this.viewConverter = viewConverter;
		this.CONVERT = new Function<CMCard, MenuElement>() {

			@Override
			public MenuElement apply(final CMCard input) {
				return converter.convert(input);
			}
		};
	}

	@Override
	protected Store<MenuElement> delegate() {
		return delegate;
	}

	public Iterable<MenuElement> readAndFilter(final String groupName, final GroupFetcher groupFetcher) {

		final Iterable<CMCard> menuCards = fetchMenuCardsForGroup(groupName);
		final CMGroup group = groupFetcher.fetchGroupWithName(groupName);

		final MenuCardFilter menuCardFilter = new MenuCardFilter(dataView, group, new Supplier<PrivilegeContext>() {

			@Override
			public PrivilegeContext get() {
				return operationUser.getPrivilegeContext();
			}
		}, viewConverter);
		final Iterable<CMCard> readableMenuCards = menuCardFilter.filterReadableMenuCards(menuCards);
		return Iterables.transform(readableMenuCards, CONVERT);
	}

	private Iterable<CMCard> fetchMenuCardsForGroup(final String groupName) {
		final List<CMCard> menuCards = Lists.newArrayList();
		final CMClass menuClass = dataView.findClass(MENU_CLASS_NAME);
		final CMQueryResult result = dataView.select(anyAttribute(menuClass)) //
				.from(menuClass) //
				.where(condition(attribute(menuClass, GROUP_NAME_ATTRIBUTE), eq(groupName))) //
				.run();
		for (final CMQueryRow row : result) {
			menuCards.add(row.getCard(menuClass));
		}
		return menuCards;
	}

}
