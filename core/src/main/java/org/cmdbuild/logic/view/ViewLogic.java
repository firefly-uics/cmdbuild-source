package org.cmdbuild.logic.view;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.View;
import org.cmdbuild.privileges.GrantCleaner;
import org.springframework.beans.factory.annotation.Autowired;

public class ViewLogic implements Logic {

	private final CMDataView dataView;
	private final Store<View> store;
	private final OperationUser operationUser;
	private final GrantCleaner grantCleaner;

	@Autowired
	public ViewLogic( //
			final CMDataView dataView, //
			final ViewConverter converter, //
			final OperationUser operationUser //
	) {
		this.dataView = dataView;
		this.store = new DataViewStore<View>(dataView, converter);
		this.operationUser = operationUser;
		this.grantCleaner = new GrantCleaner(dataView);
	}

	public List<View> fetchViewsOfAllTypes() {
		final List<View> views = new ArrayList<View>();
		for (final View view : store.list()) {
			if ((operationUser.hasAdministratorPrivileges() || operationUser.hasReadAccess(view))
					&& isActive(view.getSourceClassName())) {
				views.add(view);
			}
		}
		return views;
	}

	private boolean isActive(final String sourceClassName) {
		final CMClass clazz = dataView.findClass(sourceClassName);
		return clazz.isActive();
	}

	public List<View> read(final View.ViewType type) {
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

	public void delete(final Long id) {
		store.delete(getFakeStorable(id));
		grantCleaner.deleteGrantReferingTo(id);
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
