package org.cmdbuild.privileges.predicates;

import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_OBJECT_ID_ATTRIBUTE;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.model.View;

import com.google.common.base.Predicate;

public class IsReadableView implements Predicate<CMCard> {

	private final PrivilegeContext privilegeContext;
	private final CMDataView dataView;

	public IsReadableView(final CMDataView view, final PrivilegeContext privilegeContext) {
		this.privilegeContext = privilegeContext;
		this.dataView = view;
	}

	@Override
	public boolean apply(final CMCard menuCard) {
		final Integer viewId = menuCard.get(ELEMENT_OBJECT_ID_ATTRIBUTE, Integer.class);
		if (viewId == null) {
			return false;
		}
		final StorableConverter<View> converter = new ViewConverter();
		final DataViewStore<View> store = new DataViewStore<View>(dataView, converter);
		final View fetchedView = store.read(new Storable() {
			@Override
			public String getIdentifier() {
				return viewId.toString();
			}
		});
		if (fetchedView == null) {
			return false;
		}
		return privilegeContext.hasReadAccess(fetchedView);
	}

}
