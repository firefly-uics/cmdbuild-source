package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.services.store.menu.Comparators.byIndex;

import org.cmdbuild.service.rest.Menu;
import org.cmdbuild.service.rest.dto.MenuDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.services.store.menu.MenuItem;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

public class CxfMenu extends CxfService implements Menu {

	private static final Function<MenuItem, MenuDetail> MENU_ITEM_TO_MENU_DETAIL = new Function<MenuItem, MenuDetail>() {

		@Override
		public MenuDetail apply(final MenuItem input) {
			return MenuDetail.newInstance() //
					.withType(input.getType().getValue()) // TODO translate
					.withIndex(input.getIndex()) //
					.withObjectType(input.getReferedClassName()) //
					.withObjectId(input.getReferencedElementId().toString()) //
					.withObjectDescription(input.getDescription()) //
					.withChildren( //
							from( //
									Ordering.from(byIndex()) //
											.sortedCopy(input.getChildren()) //
							) //
							.transform(MENU_ITEM_TO_MENU_DETAIL) //
									.toList()) //
					.build();
		}

	};

	@Override
	public SimpleResponse<MenuDetail> read() {
		final String group = currentGroup().getName();
		final MenuItem menuItem = menuLogic().read(group);
		final MenuDetail element = MENU_ITEM_TO_MENU_DETAIL.apply(menuItem);
		return SimpleResponse.<MenuDetail> newInstance() //
				.withElement(element) //
				.build();
	}

}
