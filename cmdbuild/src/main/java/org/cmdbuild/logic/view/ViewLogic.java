package org.cmdbuild.logic.view;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.model.View;
import org.cmdbuild.services.store.DataViewStore;
import org.cmdbuild.services.store.DataViewStore.StorableConverter;
import org.cmdbuild.services.store.Store;
import org.cmdbuild.services.store.Store.Storable;

public class ViewLogic implements Logic {
	private final Store<View> store;

	public ViewLogic() {
		store = buildStore();
	}

	public List<View> fetchViewsOfAllTypes() {
		final List<View> views = new ArrayList<View>();
		final OperationUser operationUser = TemporaryObjectsBeforeSpringDI.getOperationUser();
		for (View view : store.list()) {
			if (operationUser.hasAdministratorPrivileges() || operationUser.hasReadAccess(view)) {
				views.add(view);
			}
		}
		return views;
	}

	public List<View> read(View.ViewType type) {
		final List<View> views = new ArrayList<View>();
		for (final View view : fetchViewsOfAllTypes()) {
			if (view.getType().equals(type)) {
				views.add(view);
			}
		}
		return views;
	}

	public View read(final Long id) {
		return store.read(getFakeStorable(id));
	}

	public void create(final View view) {
		store.create(view);
	}

	public void update(final View view) {
		store.update(view);
	}

	/**
	 * TODO: delete also privileges that refers to the deleted view
	 */
	public void delete(final Long id) {
		store.delete(getFakeStorable(id));
	}

	private Store<View> buildStore() {
		StorableConverter<View> converter = new ViewConverter();
		return new DataViewStore<View>(TemporaryObjectsBeforeSpringDI.getSystemView(), converter);
	}

	private Storable getFakeStorable(final Long id) {
		return new Storable() {
			@Override
			public String getIdentifier() {
				return id.toString();
			}
		};
	}
}
