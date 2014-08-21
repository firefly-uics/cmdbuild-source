package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.services.store.menu.Comparators.byIndex;

import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.service.rest.Menu;
import org.cmdbuild.service.rest.dto.MenuDetail;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.services.store.menu.MenuItem;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Ordering;

public class CxfMenu implements Menu {

	private static final Function<MenuItem, MenuDetail> MENU_ITEM_TO_MENU_DETAIL = new Function<MenuItem, MenuDetail>() {

		@Override
		public MenuDetail apply(final MenuItem input) {
			return MenuDetail.newInstance() //
					.withType(input.getType().getValue()) // TODO translate
					.withIndex(Long.valueOf(input.getIndex())) //
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

	private final Supplier<String> currentGroupSupplier;
	private final MenuLogic menuLogic;

	public CxfMenu(final Supplier<String> currentGroupSupplier, final MenuLogic menuLogic) {
		this.currentGroupSupplier = currentGroupSupplier;
		this.menuLogic = menuLogic;
	}

	@Override
	public SimpleResponse<MenuDetail> read() {
		final String group = currentGroupSupplier.get();
		final MenuItem menuItem = menuLogic.read(group);
		final MenuDetail element = MENU_ITEM_TO_MENU_DETAIL.apply(menuItem);
		return SimpleResponse.<MenuDetail> newInstance() //
				.withElement(element) //
				.build();
	}

}
