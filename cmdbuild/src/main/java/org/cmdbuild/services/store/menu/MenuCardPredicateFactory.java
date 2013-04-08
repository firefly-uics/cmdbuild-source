package org.cmdbuild.services.store.menu;

import static org.cmdbuild.services.store.menu.MenuConstants.MENU_CLASS_NAME;
import static org.cmdbuild.services.store.menu.MenuConstants.TYPE_ATTRIBUTE;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.privileges.predicates.IsAlwaysReadable;
import org.cmdbuild.privileges.predicates.IsReadableClass;
import org.cmdbuild.privileges.predicates.IsReadableDashboard;
import org.cmdbuild.services.store.menu.MenuStore.MenuItemType;

import com.google.common.base.Predicate;

public class MenuCardPredicateFactory {

	private final CMGroup group;
	private final CMDataView view;

	public MenuCardPredicateFactory(final CMDataView view, final CMGroup group) {
		this.group = group;
		this.view = view;
	}

	// TODO: change it (privileges on processes and reports)
	public Predicate<CMCard> getPredicate(final CMCard menuCard) {
		Validate.isTrue(menuCard.getType().getName().equals(MENU_CLASS_NAME));
		if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.CLASS.getValue())) {
			return new IsReadableClass(view, group);
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.FOLDER.getValue())) {
			return new IsAlwaysReadable();
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.ROOT.getValue())) {
			return new IsAlwaysReadable();
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.PROCESS.getValue())) {
			return new IsAlwaysReadable();
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.REPORT_CSV.getValue())) {
			return new IsAlwaysReadable();
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.REPORT_PDF.getValue())) {
			return new IsAlwaysReadable();
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.DASHBOARD.getValue())) {
			return new IsReadableDashboard(view, group);
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.VIEW.getValue())) {
			// FIXME: implement a IsReadableView predicate
			return new IsAlwaysReadable();
		}

		throw new IllegalArgumentException();
	}

}
