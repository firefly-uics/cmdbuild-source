package org.cmdbuild.logic.view;

import static org.cmdbuild.data.store.Storables.storableOf;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.View;
import org.cmdbuild.model._View.ViewType;
import org.cmdbuild.model._View;
import org.cmdbuild.privileges.GrantCleaner;

public class ViewLogic implements Logic {

	private final CMDataView dataView;
	private final Store<_View> store;
	private final OperationUser operationUser;
	private final GrantCleaner grantCleaner;

	public ViewLogic( //
			final CMDataView dataView, //
			final StorableConverter<_View> converter, //
			final OperationUser operationUser //
	) {
		this.dataView = dataView;
		this.store = DataViewStore.newInstance(dataView, converter);
		this.operationUser = operationUser;
		this.grantCleaner = new GrantCleaner(dataView);
	}

	public List<_View> fetchViewsOfAllTypes() {
		final List<_View> views = new ArrayList<_View>();
		for (final _View view : store.readAll()) {
			if ((operationUser.hasAdministratorPrivileges() || operationUser.hasReadAccess(view))) {
				if (view.getType().equals(ViewType.FILTER)) {
					if (isActive(view.getSourceClassName())) {
						views.add(view);
					}
				} else {
					views.add(view);
				}
			}
		}
		return views;
	}

	private boolean isActive(final String sourceClassName) {
		final CMClass clazz = dataView.findClass(sourceClassName);
		return clazz.isActive();
	}

	public List<_View> read(final View.ViewType type) {
		final List<_View> views = new ArrayList<_View>();
		for (final _View view : fetchViewsOfAllTypes()) {
			if (view.getType().equals(type)) {
				views.add(view);
			}
		}
		return views;
	}

	public _View read(final Long id) {
		return store.read(storableOf(id));
	}

	public void create(final _View view) {
		store.create(view);
	}

	public void update(final _View view) {
		store.update(view);
	}

	public void delete(final Long id) {
		store.delete(storableOf(id));
		grantCleaner.deleteGrantReferingTo(id);
	}

}
